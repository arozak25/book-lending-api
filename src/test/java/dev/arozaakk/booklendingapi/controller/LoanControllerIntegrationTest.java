package dev.arozaakk.booklendingapi.controller;

import static dev.arozaakk.booklendingapi.common.RestDocs.prettyDocument;
import static dev.arozaakk.booklendingapi.common.SqlScriptPaths.CLEANUP_SQL;
import static dev.arozaakk.booklendingapi.factory.BookFactory.createBookCreate;
import static dev.arozaakk.booklendingapi.factory.LoanFactory.createLoanCreate;
import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberCreate;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.arozaakk.booklendingapi.controller.resource.LoanResource;
import dev.arozaakk.booklendingapi.model.Book;
import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import dev.arozaakk.booklendingapi.model.Member;
import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
import dev.arozaakk.booklendingapi.service.BookService;
import dev.arozaakk.booklendingapi.service.LoanService;
import dev.arozaakk.booklendingapi.service.MemberService;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("integration-test")
@ExtendWith(RestDocumentationExtension.class)
@SqlGroup({@Sql(scripts = CLEANUP_SQL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)})
public class LoanControllerIntegrationTest {
  @Inject private WebApplicationContext context;
  @Inject private LoanService loanService;
  @Inject private BookService bookService;
  @Inject private MemberService memberService;
  @Inject private ObjectMapper objectMapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .apply(documentationConfiguration(restDocumentation))
            .build();
  }

  @Test
  void createLoan_withoutUser_shouldReturnUnauthorized() throws Exception {
    LoanCreate loanCreate = createLoanCreate();

    mockMvc
        .perform(
            post(LoanResource.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanCreate)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void completeLoan_withoutUser_shouldReturnUnauthorized() throws Exception {
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED);

    mockMvc
        .perform(
            patch(LoanResource.PATH + "/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanComplete)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void findLoans_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get(LoanResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getLoanById_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            get(LoanResource.PATH + "/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createLoan_withAdmin_shouldReturnCreatedLoan() throws Exception {
    Book book = bookService.createBook(createBookCreate());
    Member member = memberService.createMember(createMemberCreate());
    LoanCreate loanCreate = createLoanCreate(book.id(), member.id());
    long availableBeforeCreateLoan = book.availableCopies();

    mockMvc
        .perform(
            post(LoanResource.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanCreate)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isString())
        .andExpect(jsonPath("$.status").value(LoanStatus.ACTIVE.name()))
        .andExpect(jsonPath("$.book.id").value(book.id().toString()))
        .andExpect(jsonPath("$.member.id").value(member.id().toString()))
        .andExpect(jsonPath("$.borrowedAt").isString())
        .andExpect(jsonPath("$.dueDate").isString())
        .andDo(prettyDocument("loans-create"));

    Book updatedBook = bookService.getBookById(book.id());
    assertEquals(availableBeforeCreateLoan - 1, updatedBook.availableCopies());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createLoan_withAdmin_whenBookNotFound_shouldReturnNotFoundApiError() throws Exception {
    Member member = memberService.createMember(createMemberCreate());
    LoanCreate loanCreate = createLoanCreate(UUID.randomUUID(), member.id());

    mockMvc
        .perform(
            post(LoanResource.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanCreate)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("book.not.found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.path").value(LoanResource.PATH))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createLoan_withAdmin_whenMemberNotFound_shouldReturnNotFoundApiError() throws Exception {
    Book book = bookService.createBook(createBookCreate());
    LoanCreate loanCreate = createLoanCreate(book.id(), UUID.randomUUID());

    mockMvc
        .perform(
            post(LoanResource.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanCreate)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("member.not.found"))
        .andExpect(jsonPath("$.message").value("Member not found"))
        .andExpect(jsonPath("$.path").value(LoanResource.PATH))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void completeLoan_withAdmin_shouldReturnCompletedLoan() throws Exception {
    Book book = bookService.createBook(createBookCreate());
    Member member = memberService.createMember(createMemberCreate());
    Loan loan = loanService.createLoan(createLoanCreate(book.id(), member.id()));
    long availableBeforeCompleteLoan = bookService.getBookById(book.id()).availableCopies();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED);

    mockMvc
        .perform(
            patch(LoanResource.PATH + "/{id}", loan.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanComplete)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(loan.id().toString()))
        .andExpect(jsonPath("$.status").value(LoanStatus.COMPLETED.name()))
        .andExpect(jsonPath("$.book.id").value(book.id().toString()))
        .andExpect(jsonPath("$.member.id").value(member.id().toString()))
        .andExpect(jsonPath("$.completedAt").isString())
        .andDo(prettyDocument("loans-complete"));

    Book updatedBook = bookService.getBookById(book.id());
    assertEquals(availableBeforeCompleteLoan + 1, updatedBook.availableCopies());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void completeLoan_withAdmin_whenLoanNotFound_shouldReturnNotFoundApiError() throws Exception {
    UUID loanId = UUID.randomUUID();
    LoanComplete loanComplete = new LoanComplete(LoanStatus.COMPLETED);

    mockMvc
        .perform(
            patch(LoanResource.PATH + "/{id}", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanComplete)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("loan.not.found"))
        .andExpect(jsonPath("$.message").value("Loan not found."))
        .andExpect(jsonPath("$.path").value(LoanResource.PATH + "/" + loanId))
        .andExpect(jsonPath("$.timestamp").isString());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void findLoans_withAdmin_shouldReturnLoans() throws Exception {
    Book firstBook =
        bookService.createBook(createBookCreate("Dune", "Frank Herbert", "9780441172719", 4L));
    Member firstMember =
        memberService.createMember(createMemberCreate("John Doe", "john.doe@example.com"));
    Loan firstLoan = loanService.createLoan(createLoanCreate(firstBook.id(), firstMember.id()));

    Book secondBook =
        bookService.createBook(createBookCreate("1984", "George Orwell", "9780451524935", 6L));
    Member secondMember =
        memberService.createMember(createMemberCreate("Jane Doe", "jane.doe@example.com"));
    Loan secondLoan = loanService.createLoan(createLoanCreate(secondBook.id(), secondMember.id()));

    mockMvc
        .perform(get(LoanResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(
            jsonPath("$[*].id", hasItems(firstLoan.id().toString(), secondLoan.id().toString())))
        .andDo(prettyDocument("loans-list"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getLoanById_withAdmin_shouldReturnLoan() throws Exception {
    Book book = bookService.createBook(createBookCreate());
    Member member = memberService.createMember(createMemberCreate());
    Loan loan = loanService.createLoan(createLoanCreate(book.id(), member.id()));

    mockMvc
        .perform(
            get(LoanResource.PATH + "/{id}", loan.id()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(loan.id().toString()))
        .andExpect(jsonPath("$.status").value(LoanStatus.ACTIVE.name()))
        .andExpect(jsonPath("$.book.id").value(book.id().toString()))
        .andExpect(jsonPath("$.member.id").value(member.id().toString()))
        .andExpect(jsonPath("$.borrowedAt").isString())
        .andExpect(jsonPath("$.dueDate").isString())
        .andDo(prettyDocument("loans-get-by-id"));
  }
}
