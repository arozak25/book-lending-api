package dev.arozaakk.booklendingapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookUpdate(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String isbn,
    @NotNull Long additionalCopies) {}
