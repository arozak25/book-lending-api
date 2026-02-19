package dev.arozaakk.booklendingapi.service.impl;

import static dev.arozaakk.booklendingapi.factory.BookFactory.createBookEntity;
import static dev.arozaakk.booklendingapi.factory.LoanFactory.createLoanCreate;
import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberEntity;
import static dev.arozaakk.booklendingapi.utils.DateUtils.UTC_ZONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.arozaakk.booklendingapi.configuration.LoanRulesProperties;
import dev.arozaakk.booklendingapi.entity.BookEntity;
import dev.arozaakk.booklendingapi.entity.LoanEntity;
import dev.arozaakk.booklendingapi.entity.MemberEntity;
import dev.arozaakk.booklendingapi.exceptions.APIValidationException;
import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import dev.arozaakk.booklendingapi.repository.BookRepository;
import dev.arozaakk.booklendingapi.repository.LoanRepository;
import dev.arozaakk.booklendingapi.repository.MemberRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {
  @Mock private LoanRepository loanRepository;
  @Mock private BookRepository bookRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private LoanRulesProperties loanRulesProperties;

  @InjectMocks private LoanServiceImpl loanService;

  @Test
  void createLoan_whenBookHasNoAvailableCopies_shouldThrowException() {
    BookEntity bookEntity =
        createBookEntity(
            UUID.randomUUID(), "Clean Architecture", "Robert C. Martin", "9780134494166", 5L, 0L);
    MemberEntity memberEntity = createMemberEntity();
    LoanCreate loanCreate =
        createLoanCreate(bookEntity.getBookUuid(), memberEntity.getMemberUuid());

    when(bookRepository.findFirstByBookUuid(bookEntity.getBookUuid()))
        .thenReturn(Optional.of(bookEntity));
    when(memberRepository.findFirstByMemberUuid(memberEntity.getMemberUuid()))
        .thenReturn(Optional.of(memberEntity));

    assertThrows(APIValidationException.class, () -> loanService.createLoan(loanCreate));
    verify(loanRepository, never()).save(any(LoanEntity.class));
  }

  @Test
  void createLoan_whenMemberHasMaximumActiveLoans_shouldThrowException() {
    BookEntity bookEntity = createBookEntity();
    MemberEntity memberEntity = createMemberEntity();
    LoanCreate loanCreate =
        createLoanCreate(bookEntity.getBookUuid(), memberEntity.getMemberUuid());

    when(bookRepository.findFirstByBookUuid(bookEntity.getBookUuid()))
        .thenReturn(Optional.of(bookEntity));
    when(memberRepository.findFirstByMemberUuid(memberEntity.getMemberUuid()))
        .thenReturn(Optional.of(memberEntity));
    when(loanRepository.existsByMemberEntityAndStatusAndDueDateTimeBefore(
            eq(memberEntity), eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
        .thenReturn(false);
    when(loanRepository.countByMemberEntityAndStatus(memberEntity, LoanStatus.ACTIVE))
        .thenReturn(3L);
    when(loanRulesProperties.getMaxActiveLoansPerMember()).thenReturn(3L);

    assertThrows(APIValidationException.class, () -> loanService.createLoan(loanCreate));
    verify(loanRepository, never()).save(any(LoanEntity.class));
  }

  @Test
  void createLoan_whenMemberHasOverdueLoan_shouldThrowException() {
    BookEntity bookEntity = createBookEntity();
    MemberEntity memberEntity = createMemberEntity();
    LoanCreate loanCreate =
        createLoanCreate(bookEntity.getBookUuid(), memberEntity.getMemberUuid());

    when(bookRepository.findFirstByBookUuid(bookEntity.getBookUuid()))
        .thenReturn(Optional.of(bookEntity));
    when(memberRepository.findFirstByMemberUuid(memberEntity.getMemberUuid()))
        .thenReturn(Optional.of(memberEntity));
    when(loanRepository.existsByMemberEntityAndStatusAndDueDateTimeBefore(
            eq(memberEntity), eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
        .thenReturn(true);

    assertThrows(APIValidationException.class, () -> loanService.createLoan(loanCreate));
    verify(loanRepository, never()).save(any(LoanEntity.class));
  }

  @Test
  void createLoan_shouldUseConfiguredDuration() {
    BookEntity bookEntity = createBookEntity();
    MemberEntity memberEntity = createMemberEntity();
    LoanCreate loanCreate =
        createLoanCreate(bookEntity.getBookUuid(), memberEntity.getMemberUuid());

    when(bookRepository.findFirstByBookUuid(bookEntity.getBookUuid()))
        .thenReturn(Optional.of(bookEntity));
    when(memberRepository.findFirstByMemberUuid(memberEntity.getMemberUuid()))
        .thenReturn(Optional.of(memberEntity));
    when(loanRepository.existsByMemberEntityAndStatusAndDueDateTimeBefore(
            eq(memberEntity), eq(LoanStatus.ACTIVE), any(LocalDateTime.class)))
        .thenReturn(false);
    when(loanRepository.countByMemberEntityAndStatus(memberEntity, LoanStatus.ACTIVE))
        .thenReturn(0L);
    when(loanRulesProperties.getDurationDays()).thenReturn(14L);
    when(loanRulesProperties.getMaxActiveLoansPerMember()).thenReturn(3L);
    when(loanRepository.save(any(LoanEntity.class)))
        .thenAnswer(
            invocation -> {
              LoanEntity saved = invocation.getArgument(0);
              saved.setLoanUuid(UUID.randomUUID());
              return saved;
            });

    Loan loan = loanService.createLoan(loanCreate);

    assertNotNull(loan.id());
    long loanDurationInHours = Duration.between(loan.borrowedAt(), loan.dueDate()).toHours();

    assertTrue(loanDurationInHours >= 335 && loanDurationInHours <= 337);
    assertEquals(LoanStatus.ACTIVE, loan.status());
    verify(loanRulesProperties).getDurationDays();
  }

  @Test
  void completeLoan_whenStatusCompletedAndNowAfterDueDate_shouldThrowException() {
    LocalDateTime nowUtc = LocalDateTime.now(UTC_ZONE);
    LoanEntity loanEntity = createActiveLoanEntity(nowUtc.minusMinutes(10), 2L);
    UUID loanId = loanEntity.getLoanUuid();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED);

    when(loanRepository.findFirstByLoanUuid(loanId)).thenReturn(Optional.of(loanEntity));

    APIValidationException exception =
        assertThrows(
            APIValidationException.class, () -> loanService.completeLoan(loanId, loanComplete));

    assertEquals("loan.status.completed.invalid", exception.getKey());
    verify(loanRepository, never()).save(any(LoanEntity.class));
  }

  @Test
  void completeLoan_whenStatusCompletedLateAndNowOnOrBeforeDueDate_shouldThrowException() {
    LocalDateTime nowUtc = LocalDateTime.now(UTC_ZONE);
    LoanEntity loanEntity = createActiveLoanEntity(nowUtc.plusMinutes(10), 2L);
    UUID loanId = loanEntity.getLoanUuid();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED_LATE);

    when(loanRepository.findFirstByLoanUuid(loanId)).thenReturn(Optional.of(loanEntity));

    APIValidationException exception =
        assertThrows(
            APIValidationException.class, () -> loanService.completeLoan(loanId, loanComplete));

    assertEquals("loan.status.completed_late.invalid", exception.getKey());
    verify(loanRepository, never()).save(any(LoanEntity.class));
  }

  @Test
  void completeLoan_whenStatusCompletedAndNowOnOrBeforeDueDate_shouldCompleteLoan() {
    LocalDateTime nowUtc = LocalDateTime.now(UTC_ZONE);
    LoanEntity loanEntity = createActiveLoanEntity(nowUtc.plusMinutes(10), 2L);
    UUID loanId = loanEntity.getLoanUuid();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED);

    when(loanRepository.findFirstByLoanUuid(loanId)).thenReturn(Optional.of(loanEntity));
    when(loanRepository.save(any(LoanEntity.class)))
        .thenAnswer(
            invocation -> {
              LoanEntity saved = invocation.getArgument(0);
              return saved;
            });

    Loan loan = loanService.completeLoan(loanId, loanComplete);

    assertEquals(LoanStatus.COMPLETED, loan.status());
    assertNotNull(loan.completedAt());
    assertEquals(3L, loanEntity.getBookEntity().getAvailableCopies());
    verify(loanRepository).save(loanEntity);
  }

  @Test
  void completeLoan_whenStatusCompletedLateAndNowAfterDueDate_shouldCompleteLoan() {
    LocalDateTime nowUtc = LocalDateTime.now(UTC_ZONE);
    LoanEntity loanEntity = createActiveLoanEntity(nowUtc.minusMinutes(10), 2L);
    UUID loanId = loanEntity.getLoanUuid();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED_LATE);

    when(loanRepository.findFirstByLoanUuid(loanId)).thenReturn(Optional.of(loanEntity));
    when(loanRepository.save(any(LoanEntity.class)))
        .thenAnswer(
            invocation -> {
              LoanEntity saved = invocation.getArgument(0);
              return saved;
            });

    Loan loan = loanService.completeLoan(loanId, loanComplete);

    assertEquals(LoanStatus.COMPLETED_LATE, loan.status());
    assertNotNull(loan.completedAt());
    assertEquals(3L, loanEntity.getBookEntity().getAvailableCopies());
    verify(loanRepository).save(loanEntity);
  }

  private LoanEntity createActiveLoanEntity(LocalDateTime dueDateTime, long availableCopies) {
    BookEntity bookEntity =
        createBookEntity(
            UUID.randomUUID(),
            "Clean Architecture",
            "Robert C. Martin",
            "9780134494166",
            5L,
            availableCopies);
    MemberEntity memberEntity = createMemberEntity();
    LoanEntity loanEntity = new LoanEntity();
    loanEntity.setLoanUuid(UUID.randomUUID());
    loanEntity.setStatus(LoanStatus.ACTIVE);
    loanEntity.setBookEntity(bookEntity);
    loanEntity.setMemberEntity(memberEntity);
    loanEntity.setBorrowedDateTime(LocalDateTime.now(UTC_ZONE).minusDays(1));
    loanEntity.setDueDateTime(dueDateTime);
    return loanEntity;
  }
}
