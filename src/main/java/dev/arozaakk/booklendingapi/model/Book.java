package dev.arozaakk.booklendingapi.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Book(
    UUID id,
    String title,
    String author,
    String isbn,
    Long totalCopies,
    Long availableCopies,
    ZonedDateTime createdDateTime,
    ZonedDateTime updatedDateTime) {}
