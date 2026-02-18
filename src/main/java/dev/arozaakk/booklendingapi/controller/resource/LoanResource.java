package dev.arozaakk.booklendingapi.controller.resource;

import dev.arozaakk.booklendingapi.model.Loan;
import dev.arozaakk.booklendingapi.model.LoanComplete;
import dev.arozaakk.booklendingapi.model.LoanCreate;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping(LoanResource.PATH)
public interface LoanResource {
  String PATH = "/loans";

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  Loan createLoan(@RequestBody @Valid LoanCreate loanCreate);

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.PATCH,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Loan completeLoan(@PathVariable UUID id, @RequestBody @Valid LoanComplete loanComplete);

  @RequestMapping(
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Loan> findLoans();

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Loan getLoanById(@PathVariable UUID id);
}
