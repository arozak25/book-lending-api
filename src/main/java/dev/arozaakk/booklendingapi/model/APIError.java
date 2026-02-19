package dev.arozaakk.booklendingapi.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record APIError(
    int status, String code, String message, List<String> details, String path, Instant timestamp) {

  public APIError {
    details = details == null ? List.of() : List.copyOf(details);
  }

  public static APIError of(
      int status, String code, String message, List<String> details, String path) {
    return new APIError(
        status,
        Objects.requireNonNullElse(code, "INTERNAL_SERVER_ERROR"),
        Objects.requireNonNullElse(message, "An unexpected error occurred."),
        details,
        path,
        Instant.now());
  }
}
