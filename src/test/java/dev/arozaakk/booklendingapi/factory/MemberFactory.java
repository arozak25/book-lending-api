package dev.arozaakk.booklendingapi.factory;

import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.MemberUpdate;
import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
import java.util.UUID;

public final class MemberFactory {

  private MemberFactory() {}

  public static MemberCreate createMemberCreate() {
    return createMemberCreate("John Doe", "john.doe@example.com");
  }

  public static MemberCreate createMemberCreate(String name, String email) {
    return new MemberCreate(name, email);
  }

  public static MemberUpdate createMemberUpdate() {
    return createMemberUpdate(
        "John Doe Updated", "john.updated@example.com", MemberStatus.INACTIVE);
  }

  public static MemberUpdate createMemberUpdate(String name, String email, MemberStatus status) {
    return new MemberUpdate(name, email, status);
  }

  public static MemberEntity createMemberEntity() {
    return createMemberEntity(UUID.randomUUID(), "John Doe", "john.doe@example.com");
  }

  public static MemberEntity createMemberEntity(UUID id, String name, String email) {
    MemberEntity member = new MemberEntity();
    member.setMemberUuid(id);
    member.setName(name);
    member.setEmail(email);
    return member;
  }
}
