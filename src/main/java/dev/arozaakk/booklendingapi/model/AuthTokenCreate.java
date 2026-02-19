package dev.arozaakk.booklendingapi.model;

import jakarta.validation.constraints.NotBlank;

public record AuthTokenCreate(@NotBlank String username, @NotBlank String password) {}
