package dev.arozaakk.booklendingapi.controller;

import dev.arozaakk.booklendingapi.controller.resource.MemberResource;
import dev.arozaakk.booklendingapi.model.Member;
import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.MemberUpdate;
import dev.arozaakk.booklendingapi.service.MemberService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberResource {
  private final MemberService memberService;

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Member createMember(MemberCreate memberCreate) {
    return memberService.createMember(memberCreate);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Member getMemberById(UUID id) {
    return memberService.getMemberById(id);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public List<Member> findMembers() {
    return memberService.findMembers();
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Member updateMember(UUID id, MemberUpdate memberUpdate) {
    return memberService.updateMember(id, memberUpdate);
  }
}
