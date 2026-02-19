package dev.arozaakk.booklendingapi.controller.resource;

import dev.arozaakk.booklendingapi.model.AuthToken;
import dev.arozaakk.booklendingapi.model.AuthTokenCreate;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(AuthResource.PATH)
public interface AuthResource {
  String PATH = "/auth";

  @RequestMapping(
      value = "/token",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  AuthToken createToken(@RequestBody @Valid AuthTokenCreate authTokenCreate);
}
