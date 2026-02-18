package dev.arozaakk.booklendingapi.service;

import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import java.util.List;
import java.util.UUID;

public interface BookService {
  Book createBook(BookCreate bookCreate);

  Book getBookById(UUID id);

  List<Book> findMembers();

  Book updateBook(UUID id, BookUpdate bookUpdate);

  void deleteBook(UUID id);
}
