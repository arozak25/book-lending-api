package dev.arozaakk.booklendingapi.service;

import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import java.util.List;
import java.util.UUID;

public interface LoanService {
  Loan createLoan(LoanCreate loanCreate);

  Loan completeLoan(UUID id, LoanComplete loanComplete);

  Loan getLoanById(UUID id);

  List<Loan> findLoans();
}
