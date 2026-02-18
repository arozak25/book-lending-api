package dev.arozaakk.booklendingapi.utils;

import static java.time.ZoneOffset.UTC;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class DateUtils {
  public static ZonedDateTime toUtcZonedDateTime(LocalDateTime value) {
    return value == null ? null : value.atZone(UTC);
  }
}
