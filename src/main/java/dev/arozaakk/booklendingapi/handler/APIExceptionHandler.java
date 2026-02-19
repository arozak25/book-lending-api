package dev.arozaakk.booklendingapi.handler;

import dev.arozaakk.booklendingapi.model.APIError;
import dev.arozaakk.booklendingapi.service.APIErrorHandlingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class APIExceptionHandler {
  private final APIErrorHandlingService apiErrorHandlingService;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<APIError> handleException(
      Exception exception, HttpServletRequest httpServletRequest) {
    APIError apiError =
        apiErrorHandlingService.handleException(exception, httpServletRequest.getRequestURI());
    return ResponseEntity.status(apiError.status()).body(apiError);
  }
}
