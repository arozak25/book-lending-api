package dev.arozaakk.booklendingapi.model.mapper;

import static dev.arozaakk.booklendingapi.model.mapper.BookMapper.toBook;
import static dev.arozaakk.booklendingapi.model.mapper.MemberMapper.toMember;
import static dev.arozaakk.booklendingapi.utils.DateUtils.UTC_ZONE;
import static dev.arozaakk.booklendingapi.utils.DateUtils.toUtcZonedDateTime;

import dev.arozaakk.booklendingapi.entity.BookEntity;
import dev.arozaakk.booklendingapi.entity.LoanEntity;
import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public class LoanMapper {

  public static LoanEntity toLoanEntity(
      BookEntity bookEntity, MemberEntity memberEntity, long loanDurationInDays) {
    LocalDateTime dueDateTime =
        ZonedDateTime.now(UTC_ZONE).plusDays(loanDurationInDays).toLocalDateTime();
    return LoanEntity.builder()
        .loanUuid(UUID.randomUUID())
        .bookEntity(bookEntity)
        .memberEntity(memberEntity)
        .borrowedDateTime(LocalDateTime.now(UTC_ZONE))
        .dueDateTime(dueDateTime)
        .status(LoanStatus.ACTIVE)
        .build();
  }

  public static Loan toLoan(LoanEntity loanEntity) {
    return new Loan(
        loanEntity.getLoanUuid(),
        toBook(loanEntity.getBookEntity()),
        toMember(loanEntity.getMemberEntity()),
        loanEntity.getStatus(),
        toUtcZonedDateTime(loanEntity.getBorrowedDateTime()),
        toUtcZonedDateTime(loanEntity.getDueDateTime()),
        toUtcZonedDateTime(loanEntity.getCompletedDateTime()));
  }
}
