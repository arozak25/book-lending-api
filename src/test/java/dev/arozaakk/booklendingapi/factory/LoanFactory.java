package dev.arozaakk.booklendingapi.factory;

import dev.arozaakk.booklendingapi.model.LoanCreate;
import java.util.UUID;

public final class LoanFactory {

  private LoanFactory() {}

  public static LoanCreate createLoanCreate() {
    return createLoanCreate(UUID.randomUUID(), UUID.randomUUID());
  }

  public static LoanCreate createLoanCreate(UUID bookId, UUID memberId) {
    return new LoanCreate(bookId, memberId);
  }
}
