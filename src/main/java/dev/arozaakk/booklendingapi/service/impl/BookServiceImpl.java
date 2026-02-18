package dev.arozaakk.booklendingapi.service.impl;

import static dev.arozaakk.booklendingapi.model.mapper.BookMapper.toBook;
import static dev.arozaakk.booklendingapi.model.mapper.BookMapper.toBookEntity;

import dev.arozaakk.booklendingapi.entity.BookEntity;
import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import dev.arozaakk.booklendingapi.model.mapper.BookMapper;
import dev.arozaakk.booklendingapi.repository.BookRepository;
import dev.arozaakk.booklendingapi.service.BookService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;

  @Override
  @Transactional
  public Book createBook(BookCreate bookCreate) {
    BookEntity bookEntity = toBookEntity(bookCreate);
    return toBook(bookRepository.save(bookEntity));
  }

  @Override
  @Transactional(readOnly = true)
  public Book getBookById(UUID id) {
    return toBook(bookRepository.findFirstByBookUuid(id).orElseThrow());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Book> findMembers() {
    return bookRepository.findAll().stream().map(BookMapper::toBook).toList();
  }

  @Override
  @Transactional
  public Book updateBook(UUID id, BookUpdate bookUpdate) {
    BookEntity bookEntity = bookRepository.findFirstByBookUuid(id).orElseThrow();
    bookEntity.setTitle(bookUpdate.title());
    bookEntity.setAuthor(bookUpdate.author());
    bookEntity.setIsbn(bookUpdate.isbn());
    bookEntity.setTotalCopies(bookEntity.getTotalCopies() + bookUpdate.additionalCopies());
    bookEntity.setAvailableCopies(bookEntity.getAvailableCopies() + bookUpdate.additionalCopies());

    return toBook(bookRepository.save(bookEntity));
  }

  @Override
  @Transactional
  public void deleteBook(UUID id) {
    BookEntity bookEntity = bookRepository.findFirstByBookUuid(id).orElseThrow();
    bookRepository.delete(bookEntity);
  }
}
