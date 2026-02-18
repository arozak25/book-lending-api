package dev.arozaakk.booklendingapi.model;

import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberUpdate(
    @NotBlank String name, @NotBlank String email, @NotNull MemberStatus status) {}
