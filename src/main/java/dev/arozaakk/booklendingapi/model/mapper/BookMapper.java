package dev.arozaakk.booklendingapi.model.mapper;

import static dev.arozaakk.booklendingapi.utils.DateUtils.toUtcZonedDateTime;

import dev.arozaakk.booklendingapi.entity.BookEntity;
import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import java.util.UUID;

public class BookMapper {

  public static BookEntity toBookEntity(BookCreate bookCreate) {
    return BookEntity.builder()
        .bookUuid(UUID.randomUUID())
        .title(bookCreate.title())
        .author(bookCreate.author())
        .isbn(bookCreate.isbn())
        .totalCopies(bookCreate.totalCopies())
        .availableCopies(bookCreate.totalCopies())
        .build();
  }

  public static Book toBook(BookEntity bookEntity) {
    return new Book(
        bookEntity.getBookUuid(),
        bookEntity.getTitle(),
        bookEntity.getAuthor(),
        bookEntity.getIsbn(),
        bookEntity.getTotalCopies(),
        bookEntity.getAvailableCopies(),
        toUtcZonedDateTime(bookEntity.getCreatedDateTime()),
        toUtcZonedDateTime(bookEntity.getUpdatedDateTime()));
  }
}
