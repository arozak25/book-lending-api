package dev.arozaakk.booklendingapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookCreate(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String isbn,
    @NotNull Long totalCopies) {}
