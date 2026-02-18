package dev.arozaakk.booklendingapi.repository;

import dev.arozaakk.booklendingapi.entity.LoanEntity;
import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
  Optional<LoanEntity> findFirstByLoanUuid(UUID loanUuid);

  long countByMemberEntityAndStatus(MemberEntity memberEntity, LoanStatus status);

  boolean existsByMemberEntityAndStatusAndDueDateTimeBefore(
      MemberEntity memberEntity, LoanStatus status, LocalDateTime dueDateTime);
}
