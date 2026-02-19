package dev.arozaakk.booklendingapi.exceptions;

import java.util.UUID;

public class MemberNotFoundException extends APIItemNotFoundException {
  public MemberNotFoundException(UUID memberId) {
    super("member.not.found", memberId);
  }
}
