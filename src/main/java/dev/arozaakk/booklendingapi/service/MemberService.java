package dev.arozaakk.booklendingapi.service;

import dev.arozaakk.booklendingapi.model.*;
import java.util.List;
import java.util.UUID;

public interface MemberService {
  Member createMember(MemberCreate memberCreate);

  Member getMemberById(UUID id);

  List<Member> findMembers();

  Member updateMember(UUID id, MemberUpdate memberUpdate);
}
