package dev.arozaakk.booklendingapi.model;

import jakarta.validation.constraints.NotBlank;

public record MemberCreate(@NotBlank String name, @NotBlank String email) {}
