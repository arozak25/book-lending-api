package dev.arozaakk.booklendingapi.controller;

import static dev.arozaakk.booklendingapi.factory.BookFactory.createBookCreate;
import static dev.arozaakk.booklendingapi.factory.BookFactory.createBookUpdate;
import static dev.arozaakk.booklendingapi.testsupport.SqlScriptPaths.CLEANUP_SQL;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.arozaakk.booklendingapi.controller.resource.BookResource;
import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.BookCreate;
import dev.arozaakk.booklendingapi.model.BookUpdate;
import dev.arozaakk.booklendingapi.service.BookService;
import jakarta.inject.Inject;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SqlGroup({@Sql(scripts = CLEANUP_SQL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)})
public class BookControllerIntegrationTest {
  @Inject private MockMvc mockMvc;
  @Inject private BookService bookService;
  @Inject private ObjectMapper objectMapper;

  @Test
  void createBook_withoutUser_shouldReturnUnauthorized() throws Exception {
    BookCreate bookCreate = createBookCreate();

    mockMvc
        .perform(
            post(BookResource.PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookCreate)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getBookById_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            get(BookResource.PATH + "/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void findBooks_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get(BookResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateBook_withoutUser_shouldReturnUnauthorized() throws Exception {
    BookUpdate bookUpdate = createBookUpdate();

    mockMvc
        .perform(
            put(BookResource.PATH + "/{id}", UUID.randomUUID())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookUpdate)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deleteBook_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(delete(BookResource.PATH + "/{id}", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createBook_withAdmin_shouldReturnCreatedBook() throws Exception {
    BookCreate bookCreate = createBookCreate();

    mockMvc
        .perform(
            post(BookResource.PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookCreate)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value(bookCreate.title()))
        .andExpect(jsonPath("$.author").value(bookCreate.author()))
        .andExpect(jsonPath("$.isbn").value(bookCreate.isbn()))
        .andExpect(jsonPath("$.totalCopies").value(10))
        .andExpect(jsonPath("$.availableCopies").value(10))
        .andExpect(jsonPath("$.id").isString());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getBookById_withAdmin_shouldReturnBook() throws Exception {
    Book createdBook = bookService.createBook(createBookCreate());
    UUID id = createdBook.id();

    mockMvc
        .perform(get(BookResource.PATH + "/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.title").value(createdBook.title()))
        .andExpect(jsonPath("$.author").value(createdBook.author()))
        .andExpect(jsonPath("$.isbn").value(createdBook.isbn()))
        .andExpect(jsonPath("$.totalCopies").value(createdBook.totalCopies()))
        .andExpect(jsonPath("$.availableCopies").value(createdBook.availableCopies()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void findBooks_withAdmin_shouldReturnBooks() throws Exception {
    Book firstBook =
        bookService.createBook(createBookCreate("Dune", "Frank Herbert", "9780441172719", 4L));
    Book secondBook =
        bookService.createBook(createBookCreate("1984", "George Orwell", "9780451524935", 6L));

    mockMvc
        .perform(get(BookResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(
            jsonPath("$[*].id", hasItems(firstBook.id().toString(), secondBook.id().toString())));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateBook_withAdmin_shouldReturnUpdatedBook() throws Exception {
    Book createdBook = bookService.createBook(createBookCreate());
    UUID id = createdBook.id();
    BookUpdate bookUpdate = createBookUpdate();

    mockMvc
        .perform(
            put(BookResource.PATH + "/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookUpdate)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.title").value(bookUpdate.title()))
        .andExpect(jsonPath("$.author").value(bookUpdate.author()))
        .andExpect(jsonPath("$.isbn").value(bookUpdate.isbn()))
        .andExpect(jsonPath("$.totalCopies").value(12))
        .andExpect(jsonPath("$.availableCopies").value(12));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteBook_withAdmin_shouldReturnNoContent() throws Exception {
    Book createdBook = bookService.createBook(createBookCreate());
    UUID id = createdBook.id();

    mockMvc
        .perform(delete(BookResource.PATH + "/{id}", id).with(csrf()))
        .andExpect(status().isNoContent());
    assertThrows(NoSuchElementException.class, () -> bookService.getBookById(id));
  }
}
