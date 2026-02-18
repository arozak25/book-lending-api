package dev.arozaakk.booklendingapi.model;

import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
import java.time.ZonedDateTime;
import java.util.UUID;

public record Member(
    UUID id,
    String name,
    String email,
    MemberStatus status,
    ZonedDateTime createdDateTime,
    ZonedDateTime updatedDateTime) {}
