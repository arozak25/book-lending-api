package dev.arozaakk.booklendingapi.model.mapper;

import static dev.arozaakk.booklendingapi.utils.DateUtils.toUtcZonedDateTime;

import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.model.Member;
import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
import java.util.UUID;

public class MemberMapper {
  public static MemberEntity toMemberEntity(MemberCreate memberCreate) {
    return MemberEntity.builder()
        .memberUuid(UUID.randomUUID())
        .name(memberCreate.name())
        .email(memberCreate.email())
        .status(MemberStatus.ACTIVE)
        .build();
  }

  public static Member toMember(MemberEntity memberEntity) {
    return new Member(
        memberEntity.getMemberUuid(),
        memberEntity.getName(),
        memberEntity.getEmail(),
        memberEntity.getStatus(),
        toUtcZonedDateTime(memberEntity.getCreatedDateTime()),
        toUtcZonedDateTime(memberEntity.getUpdatedDateTime()));
  }
}
