package dev.arozaakk.booklendingapi.controller;

import dev.arozaakk.booklendingapi.controller.resource.LoanResource;
import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import dev.arozaakk.booklendingapi.service.LoanService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoanController implements LoanResource {
  private final LoanService loanService;

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Loan createLoan(LoanCreate loanCreate) {
    return loanService.createLoan(loanCreate);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Loan completeLoan(UUID id, LoanComplete loanComplete) {
    return loanService.completeLoan(id, loanComplete);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public List<Loan> findLoans() {
    return loanService.findLoans();
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Loan getLoanById(UUID id) {
    return loanService.getLoanById(id);
  }
}
