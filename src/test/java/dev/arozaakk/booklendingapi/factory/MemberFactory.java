package dev.arozaakk.booklendingapi.factory;

import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.MemberUpdate;
import dev.arozaakk.booklendingapi.model.enums.MemberStatus;

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
}
