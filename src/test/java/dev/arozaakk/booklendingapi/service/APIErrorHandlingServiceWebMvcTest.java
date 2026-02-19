package dev.arozaakk.booklendingapi.service;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.arozaakk.booklendingapi.exceptions.APIValidationException;
import dev.arozaakk.booklendingapi.exceptions.BookNotFoundException;
import dev.arozaakk.booklendingapi.handler.APIExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class APIErrorHandlingServiceWebMvcTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage("book.not.found", Locale.ENGLISH, "Book not found");
    messageSource.addMessage(
        "book.no.available.copies", Locale.ENGLISH, "Book has no available copies");

    APIErrorHandlingService apiErrorHandlingService = new APIErrorHandlingService(messageSource);
    APIExceptionHandler apiExceptionHandler = new APIExceptionHandler(apiErrorHandlingService);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(new ErrorController())
            .setControllerAdvice(apiExceptionHandler)
            .build();
  }

  @Test
  void whenResourceNotFound_shouldReturn404ApiError() throws Exception {
    mockMvc
        .perform(get("/error-test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("book.not.found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.path").value("/error-test/not-found"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void whenBusinessRuleViolation_shouldReturn400ApiError() throws Exception {
    mockMvc
        .perform(get("/error-test/business-rule"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("book.no.available.copies"))
        .andExpect(jsonPath("$.message").value("Book has no available copies"))
        .andExpect(jsonPath("$.path").value("/error-test/business-rule"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void whenValidationFails_shouldReturn400ApiError() throws Exception {
    mockMvc
        .perform(
            post("/error-test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.details[0]", containsString("title")))
        .andExpect(jsonPath("$.path").value("/error-test/validation"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void whenTypeMismatchOccurs_shouldReturn400ApiError() throws Exception {
    mockMvc
        .perform(get("/error-test/type-mismatch").param("page", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"))
        .andExpect(jsonPath("$.message").value("page is not a valid Integer value but was 'abc'"))
        .andExpect(jsonPath("$.path").value("/error-test/type-mismatch"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void whenRequestBodyIsMalformed_shouldReturn400ApiError() throws Exception {
    mockMvc
        .perform(
            post("/error-test/malformed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"abc\""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
        .andExpect(jsonPath("$.message").value("Could not parse HTTP request body as JSON."))
        .andExpect(jsonPath("$.path").value("/error-test/malformed"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  void whenUnhandledException_shouldReturn500ApiError() throws Exception {
    mockMvc
        .perform(get("/error-test/internal"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
        .andExpect(jsonPath("$.path").value("/error-test/internal"))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @RestController
  @RequestMapping("/error-test")
  static class ErrorController {

    @GetMapping("/not-found")
    void notFound() {
      throw new BookNotFoundException(java.util.UUID.randomUUID());
    }

    @GetMapping("/business-rule")
    void businessRule() {
      throw new APIValidationException("book.no.available.copies");
    }

    @PostMapping("/validation")
    void validation(@Valid @RequestBody ValidationRequest request) {}

    @GetMapping("/type-mismatch")
    void typeMismatch(@RequestParam Integer page) {}

    @PostMapping("/malformed")
    void malformed(@RequestBody Map<String, Object> payload) {}

    @GetMapping("/internal")
    void internal() {
      throw new RuntimeException("unexpected");
    }
  }

  record ValidationRequest(@NotBlank String title) {}
}
