package dev.arozaakk.booklendingapi.service.impl;

import static dev.arozaakk.booklendingapi.model.mapper.MemberMapper.toMember;
import static dev.arozaakk.booklendingapi.model.mapper.MemberMapper.toMemberEntity;

import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.model.Member;
import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.MemberUpdate;
import dev.arozaakk.booklendingapi.model.mapper.MemberMapper;
import dev.arozaakk.booklendingapi.repository.MemberRepository;
import dev.arozaakk.booklendingapi.service.MemberService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;

  @Override
  @Transactional
  public Member createMember(MemberCreate memberCreate) {
    MemberEntity memberEntity = toMemberEntity(memberCreate);
    return toMember(memberRepository.save(memberEntity));
  }

  @Override
  @Transactional(readOnly = true)
  public Member getMemberById(UUID id) {
    return toMember(memberRepository.findFirstByMemberUuid(id).orElseThrow());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Member> findMembers() {
    return memberRepository.findAll().stream().map(MemberMapper::toMember).toList();
  }

  @Override
  @Transactional
  public Member updateMember(UUID id, MemberUpdate memberUpdate) {
    MemberEntity memberEntity = memberRepository.findFirstByMemberUuid(id).orElseThrow();
    memberEntity.setName(memberUpdate.name());
    memberEntity.setEmail(memberUpdate.email());
    memberEntity.setStatus(memberUpdate.status());

    return toMember(memberRepository.save(memberEntity));
  }
}
