package dev.arozaakk.booklendingapi.repository;

import dev.arozaakk.booklendingapi.entity.MemberEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
  Optional<MemberEntity> findFirstByMemberUuid(UUID memberUuid);
}
