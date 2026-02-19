package dev.arozaakk.booklendingapi.service.impl;

import static dev.arozaakk.booklendingapi.model.mapper.LoanMapper.toLoan;
import static dev.arozaakk.booklendingapi.model.mapper.LoanMapper.toLoanEntity;
import static dev.arozaakk.booklendingapi.utils.DateUtils.UTC_ZONE;

import dev.arozaakk.booklendingapi.configuration.LoanRulesProperties;
import dev.arozaakk.booklendingapi.entity.BookEntity;
import dev.arozaakk.booklendingapi.entity.LoanEntity;
import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.exceptions.APIValidationException;
import dev.arozaakk.booklendingapi.exceptions.BookNotFoundException;
import dev.arozaakk.booklendingapi.exceptions.LoanNotFoundException;
import dev.arozaakk.booklendingapi.exceptions.MemberNotFoundException;
import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import dev.arozaakk.booklendingapi.model.mapper.LoanMapper;
import dev.arozaakk.booklendingapi.repository.BookRepository;
import dev.arozaakk.booklendingapi.repository.LoanRepository;
import dev.arozaakk.booklendingapi.repository.MemberRepository;
import dev.arozaakk.booklendingapi.service.LoanService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

  private final LoanRepository loanRepository;
  private final BookRepository bookRepository;
  private final MemberRepository memberRepository;
  private final LoanRulesProperties loanRulesProperties;

  @Override
  @Transactional
  public Loan createLoan(LoanCreate loanCreate) {
    BookEntity bookEntity =
        bookRepository
            .findFirstByBookUuid(loanCreate.bookId())
            .orElseThrow(() -> new BookNotFoundException(loanCreate.bookId()));
    MemberEntity memberEntity =
        memberRepository
            .findFirstByMemberUuid(loanCreate.memberId())
            .orElseThrow(() -> new MemberNotFoundException(loanCreate.memberId()));
    validateLoan(memberEntity, bookEntity);

    LoanEntity loanEntity =
        loanRepository.save(
            toLoanEntity(bookEntity, memberEntity, loanRulesProperties.getDurationDays()));
    bookEntity.setAvailableCopies(bookEntity.getAvailableCopies() - 1);

    return toLoan(loanEntity);
  }

  @Override
  @Transactional
  public Loan completeLoan(UUID id, LoanComplete loanComplete) {
    LoanEntity loanEntity =
        loanRepository.findFirstByLoanUuid(id).orElseThrow(() -> new LoanNotFoundException(id));
    if (loanEntity.getStatus() != LoanStatus.ACTIVE) {
      throw new APIValidationException("loan.already.completed");
    }

    LocalDateTime completedAt = LocalDateTime.now(UTC_ZONE);
    validateCompletionStatus(loanComplete.loanStatus(), completedAt, loanEntity.getDueDateTime());

    loanEntity.setStatus(loanComplete.loanStatus());
    loanEntity.setCompletedDateTime(completedAt);

    loanRepository.save(loanEntity);

    BookEntity bookEntity = loanEntity.getBookEntity();
    bookEntity.setAvailableCopies(bookEntity.getAvailableCopies() + 1);

    return toLoan(loanEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Loan getLoanById(UUID id) {
    return toLoan(
        loanRepository.findFirstByLoanUuid(id).orElseThrow(() -> new LoanNotFoundException(id)));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Loan> findLoans() {
    return loanRepository.findAll().stream().map(LoanMapper::toLoan).toList();
  }

  private void validateCompletionStatus(
      LoanStatus loanStatus, LocalDateTime completedAt, LocalDateTime dueDateTime) {
    if ((loanStatus == LoanStatus.COMPLETED || loanStatus == LoanStatus.COMPLETED_LATE)
        && dueDateTime == null) {
      throw new APIValidationException("loan.due.date.required");
    }

    if (loanStatus == LoanStatus.COMPLETED && completedAt.isAfter(dueDateTime)) {
      throw new APIValidationException("loan.status.completed.invalid");
    }

    if (loanStatus == LoanStatus.COMPLETED_LATE && !completedAt.isAfter(dueDateTime)) {
      throw new APIValidationException("loan.status.completed_late.invalid");
    }
  }

  private void validateLoan(MemberEntity memberEntity, BookEntity bookEntity) {
    if (bookEntity.getAvailableCopies() == null || bookEntity.getAvailableCopies() <= 0) {
      throw new APIValidationException("book.no.available.copies");
    }

    LocalDateTime nowUtc = LocalDateTime.now(UTC_ZONE);
    boolean hasOverdueLoan =
        loanRepository.existsByMemberEntityAndStatusAndDueDateTimeBefore(
            memberEntity, LoanStatus.ACTIVE, nowUtc);

    if (hasOverdueLoan) {
      throw new APIValidationException("member.has.overdue.loans");
    }

    long activeLoanCount =
        loanRepository.countByMemberEntityAndStatus(memberEntity, LoanStatus.ACTIVE);
    if (activeLoanCount >= loanRulesProperties.getMaxActiveLoansPerMember()) {
      throw new APIValidationException("member.max.active.loans.reached");
    }
  }
}
