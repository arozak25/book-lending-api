package dev.arozaakk.booklendingapi.factory;

import static dev.arozaakk.booklendingapi.utils.DateUtils.toUtcZonedDateTime;

import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public final class BookFactory {

  private BookFactory() {}

  public static BookCreate createBookCreate() {
    return createBookCreate("Clean Code", "Robert C. Martin", "9780132350884", 10L);
  }

  public static BookCreate createBookCreate(
      String title, String author, String isbn, Long totalCopies) {
    return new BookCreate(title, author, isbn, totalCopies);
  }

  public static BookUpdate createBookUpdate() {
    return createBookUpdate("Clean Code (Second Edition)", "Robert C. Martin", "9780132350884", 2L);
  }

  public static BookUpdate createBookUpdate(
      String title, String author, String isbn, Long additionalCopies) {
    return new BookUpdate(title, author, isbn, additionalCopies);
  }

  public static Book createBook(
      UUID id, String title, String author, String isbn, Long totalCopies, Long availableCopies) {
    return createBook(
        id,
        title,
        author,
        isbn,
        totalCopies,
        availableCopies,
        toUtcZonedDateTime(LocalDateTime.now()),
        toUtcZonedDateTime(LocalDateTime.now()));
  }

  public static Book createBook(
      UUID id,
      String title,
      String author,
      String isbn,
      Long totalCopies,
      Long availableCopies,
      ZonedDateTime createdDateTime,
      ZonedDateTime updatedDateTime) {
    return new Book(
        id, title, author, isbn, totalCopies, availableCopies, createdDateTime, updatedDateTime);
  }
}
