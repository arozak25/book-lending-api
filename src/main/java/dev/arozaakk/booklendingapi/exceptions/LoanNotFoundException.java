package dev.arozaakk.booklendingapi.exceptions;

import java.util.UUID;

public class LoanNotFoundException extends APIItemNotFoundException {
  public LoanNotFoundException(UUID loanId) {
    super("loan.not.found", loanId);
  }
}
