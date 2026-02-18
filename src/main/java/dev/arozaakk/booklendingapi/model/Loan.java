package dev.arozaakk.booklendingapi.model;

import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import java.time.ZonedDateTime;
import java.util.UUID;

public record Loan(
    UUID id,
    Book book,
    Member member,
    LoanStatus status,
    ZonedDateTime borrowedAt,
    ZonedDateTime dueDate,
    ZonedDateTime completedAt) {}
