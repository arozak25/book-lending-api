package dev.arozaakk.booklendingapi.model;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LoanCreate(@NotNull UUID bookId, @NotNull UUID memberId) {}
