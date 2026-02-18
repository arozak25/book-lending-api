package dev.arozaakk.booklendingapi.controller;

import dev.arozaakk.booklendingapi.controller.resource.BookResource;
import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import dev.arozaakk.booklendingapi.service.BookService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookController implements BookResource {

  private final BookService bookService;

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Book createBook(BookCreate bookCreate) {
    return bookService.createBook(bookCreate);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Book getBookById(UUID id) {
    return bookService.getBookById(id);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public List<Book> findBooks() {
    return bookService.findMembers();
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public Book updateBook(UUID id, BookUpdate bookUpdate) {
    return bookService.updateBook(id, bookUpdate);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteBook(UUID id) {
    bookService.deleteBook(id);
  }
}
