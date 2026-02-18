package dev.arozaakk.booklendingapi.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class DateUtils {
  public static final ZoneId UTC_ZONE = ZoneOffset.UTC;

  private DateUtils() {}

  public static ZonedDateTime toUtcZonedDateTime(LocalDateTime value) {
    return value == null ? null : value.atZone(UTC_ZONE);
  }
}
