package dev.arozaakk.booklendingapi.controller.resource;

import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping(BookResource.PATH)
public interface BookResource {
  String PATH = "/books";

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  Book createBook(@RequestBody @Valid BookCreate bookCreate);

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Book getBookById(@PathVariable UUID id);

  @RequestMapping(
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Book> findBooks();

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Book updateBook(@PathVariable UUID id, @RequestBody @Valid BookUpdate bookUpdate);

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteBook(@PathVariable UUID id);
}
