package dev.arozaakk.booklendingapi.service;

import dev.arozaakk.booklendingapi.exceptions.APIItemNotFoundException;
import dev.arozaakk.booklendingapi.exceptions.APIValidationException;
import dev.arozaakk.booklendingapi.model.APIError;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Service
public class APIErrorHandlingService {
  private static final Logger LOG = LoggerFactory.getLogger(APIErrorHandlingService.class);

  private static final String JSON_PARSE_MESSAGE = "Could not parse HTTP request body as JSON.";
  private static final String REQUEST_NOT_READABLE_MESSAGE =
      "Could not read HTTP message. Is the request malformed or has wrong media type?";
  private final MessageSource messageSource;

  public APIErrorHandlingService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public APIError handleException(Exception exception, String path) {
    if (exception instanceof MethodArgumentNotValidException ex) {
      return handleMethodArgumentNotValidException(ex, path);
    }
    if (exception instanceof BindException ex) {
      return handleBindException(ex, path);
    }
    if (exception instanceof ConstraintViolationException ex) {
      return handleConstraintViolationException(ex, path);
    }
    if (exception instanceof MissingServletRequestParameterException ex) {
      return handleMissingServletRequestParameterException(ex, path);
    }
    if (exception instanceof MissingServletRequestPartException ex) {
      return handleMissingServletRequestPartException(ex, path);
    }
    if (exception instanceof MethodArgumentTypeMismatchException ex) {
      return handleMethodArgumentTypeMismatchException(ex, path);
    }
    if (exception instanceof HttpMessageNotReadableException ex) {
      return handleHttpMessageNotReadableException(ex, path);
    }
    if (exception instanceof HttpRequestMethodNotSupportedException ex) {
      return handleHttpRequestMethodNotSupportedException(ex, path);
    }
    if (exception instanceof HttpMediaTypeNotSupportedException ex) {
      return handleHttpMediaTypeNotSupportedException(ex, path);
    }
    if (exception instanceof AuthenticationException ex) {
      return createApiError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), path);
    }
    if (exception instanceof AccessDeniedException ex) {
      return createApiError(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), path);
    }
    if (exception instanceof ValidationException ex) {
      return handleValidationException(ex, path);
    }
    if (exception instanceof APIItemNotFoundException ex) {
      String notFoundCode = resolveNotFoundCode(ex.getKey());
      String message = resolveMessage(notFoundCode, ex.getArgs());
      return createApiError(HttpStatus.NOT_FOUND, notFoundCode, message, path);
    }
    if (exception instanceof java.util.NoSuchElementException ex) {
      return createApiError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), path);
    }
    if (exception instanceof APIValidationException ex) {
      String validationCode = resolveValidationCode(ex.getKey());
      String message = resolveMessage(validationCode, ex.getArgs());
      return createApiError(
          HttpStatus.BAD_REQUEST, validationCode, message, List.of(message), path);
    }
    return createApiError(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "An unexpected error occurred.",
        path);
  }

  private APIError handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, String path) {
    List<String> details = fieldErrorsToDetails(ex.getBindingResult().getFieldErrors());
    if (details.isEmpty()) {
      details = List.of(Objects.requireNonNullElse(ex.getMessage(), "Validation failed"));
    }

    return createApiValidationError("Validation failed", details, path);
  }

  private APIError handleBindException(BindException ex, String path) {
    List<String> details = fieldErrorsToDetails(ex.getBindingResult().getFieldErrors());
    if (details.isEmpty()) {
      details = List.of(Objects.requireNonNullElse(ex.getMessage(), "Validation failed"));
    }

    return createApiValidationError("Validation failed", details, path);
  }

  private APIError handleConstraintViolationException(
      ConstraintViolationException ex, String path) {
    List<String> details =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    violation.getPropertyPath()
                        + " "
                        + Objects.requireNonNullElse(violation.getMessage(), "is invalid"))
            .toList();

    return createApiValidationError("Validation failed", details, path);
  }

  private APIError handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex, String path) {
    String message = ex.getParameterName() + " request parameter must not be null";
    return createApiValidationError(message, List.of(message), path);
  }

  private APIError handleMissingServletRequestPartException(
      MissingServletRequestPartException ex, String path) {
    String message = ex.getRequestPartName() + " request part must not be null";
    return createApiValidationError(message, List.of(message), path);
  }

  private APIError handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, String path) {
    Throwable rootCause = getRootCause(ex);
    String rootCauseName = rootCause != null ? rootCause.getClass().getSimpleName() : "";
    String exceptionMessage = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

    String message =
        switch (rootCauseName) {
          case "JsonParseException", "StreamReadException" -> JSON_PARSE_MESSAGE;
          case "InvalidFormatException" -> getInvalidFormatMessage(rootCause);
          default -> {
            if (exceptionMessage.contains("json parse error")) {
              yield JSON_PARSE_MESSAGE;
            }
            yield REQUEST_NOT_READABLE_MESSAGE;
          }
        };

    return createApiError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", message, path);
  }

  private APIError handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, String path) {
    String message = generateInvalidTypeMessage(ex.getName(), ex.getRequiredType(), ex.getValue());
    return createApiError(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER_TYPE", message, path);
  }

  private APIError handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, String path) {
    String[] methods = ex.getSupportedMethods();
    String supportedMethods = methods == null ? "" : String.join(", ", methods);
    String message =
        String.format(
            "Method %s is not supported. Supported methods are [%s]",
            ex.getMethod(), supportedMethods);
    return createApiError(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", message, path);
  }

  private APIError handleHttpMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException ex, String path) {
    String message =
        "Content type "
            + ex.getContentType()
            + " is not supported. Supported media types are "
            + ex.getSupportedMediaTypes();
    return createApiError(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", message, path);
  }

  private APIError handleValidationException(ValidationException ex, String path) {
    String message = Objects.requireNonNullElse(ex.getMessage(), "Validation failed");
    return createApiValidationError(message, List.of(message), path);
  }

  private APIError createApiValidationError(String message, List<String> details, String path) {
    return createApiError(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        Objects.requireNonNullElse(message, "Validation failed"),
        details,
        path);
  }

  private APIError createApiError(HttpStatus status, String code, String message, String path) {
    return createApiError(status, code, message, List.of(), path);
  }

  private APIError createApiError(
      HttpStatus status, String code, String message, List<String> details, String path) {
    LOG.debug("Returning API error status={}, code={}, message={}", status, code, message);
    return APIError.of(
        status.value(),
        code,
        Objects.requireNonNullElse(message, status.getReasonPhrase()),
        details,
        path);
  }

  private String resolveValidationCode(String key) {
    if (key == null || key.isBlank()) {
      return "VALIDATION_ERROR";
    }
    return key;
  }

  private String resolveNotFoundCode(String key) {
    if (key == null || key.isBlank()) {
      return "RESOURCE_NOT_FOUND";
    }
    return key;
  }

  private String resolveMessage(String key, Object[] args) {
    if ("VALIDATION_ERROR".equals(key)) {
      return "Validation failed";
    }
    return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
  }

  private String generateInvalidTypeMessage(String fieldName, Class<?> targetType, Object value) {
    if (targetType != null && targetType.isEnum()) {
      String enumValues =
          Arrays.stream(targetType.getEnumConstants())
              .map(String::valueOf)
              .collect(Collectors.joining(", "));
      return fieldName + " must be one of [" + enumValues + "] but was '" + value + "'";
    }
    String targetName = targetType != null ? targetType.getSimpleName() : "unknown";
    return fieldName + " is not a valid " + targetName + " value but was '" + value + "'";
  }

  private String getInvalidFormatMessage(Throwable invalidFormatException) {
    String fieldInError = tryReadFieldInError(invalidFormatException);
    Class<?> targetType = tryReadTargetType(invalidFormatException);
    Object value = tryReadValue(invalidFormatException);
    return generateInvalidTypeMessage(fieldInError, targetType, value);
  }

  private String tryReadFieldInError(Throwable exception) {
    Object path = invokeMethod(exception, "getPath");
    if (path instanceof List<?> pathList && !pathList.isEmpty()) {
      List<String> fragments = new ArrayList<>();
      for (Object reference : pathList) {
        Object index = invokeMethod(reference, "getIndex");
        if (index instanceof Integer idx && idx >= 0) {
          fragments.add("[" + idx + "]");
          continue;
        }

        Object fieldName = invokeMethod(reference, "getFieldName");
        if (fieldName != null) {
          fragments.add(String.valueOf(fieldName));
        }
      }

      if (!fragments.isEmpty()) {
        StringBuilder builder = new StringBuilder();
        for (String fragment : fragments) {
          if (fragment.startsWith("[")) {
            builder.append(fragment);
          } else if (builder.length() == 0) {
            builder.append(fragment);
          } else {
            builder.append(".").append(fragment);
          }
        }
        return builder.toString();
      }
    }

    return "value";
  }

  private Class<?> tryReadTargetType(Throwable exception) {
    Object targetType = invokeMethod(exception, "getTargetType");
    return targetType instanceof Class<?> type ? type : null;
  }

  private Object tryReadValue(Throwable exception) {
    return invokeMethod(exception, "getValue");
  }

  private Object invokeMethod(Object target, String methodName) {
    if (target == null) {
      return null;
    }
    try {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private List<String> fieldErrorsToDetails(List<FieldError> fieldErrors) {
    return fieldErrors.stream()
        .map(
            error ->
                error.getField()
                    + " "
                    + Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid"))
        .toList();
  }

  private Throwable getRootCause(Throwable throwable) {
    Throwable root = throwable;
    while (root.getCause() != null && root.getCause() != root) {
      root = root.getCause();
    }
    return root;
  }
}
