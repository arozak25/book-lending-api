package dev.arozaakk.booklendingapi.controller;

import dev.arozaakk.booklendingapi.controller.resource.AuthResource;
import dev.arozaakk.booklendingapi.model.AuthToken;
import dev.arozaakk.booklendingapi.model.AuthTokenCreate;
import dev.arozaakk.booklendingapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthResource {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  @Override
  public AuthToken createToken(AuthTokenCreate authTokenCreate) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authTokenCreate.username(), authTokenCreate.password()));
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return new AuthToken(jwtService.generateToken(userDetails), "Bearer");
  }
}
