package dev.arozaakk.booklendingapi.exceptions;

import java.util.UUID;

public class BookNotFoundException extends APIItemNotFoundException {
  public BookNotFoundException(UUID bookId) {
    super("book.not.found", bookId);
  }
}
