package dev.arozaakk.booklendingapi.model;

import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import jakarta.validation.constraints.NotNull;

public record LoanComplete(@NotNull LoanStatus loanStatus) {}
