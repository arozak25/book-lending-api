package dev.arozaakk.booklendingapi.controller.resource;

import dev.arozaakk.booklendingapi.model.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping(MemberResource.PATH)
public interface MemberResource {
  String PATH = "/members";

  @RequestMapping(
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  Member createMember(@RequestBody @Valid MemberCreate memberCreate);

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Member getMemberById(@PathVariable UUID id);

  @RequestMapping(
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Member> findMembers();

  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  Member updateMember(@PathVariable UUID id, @RequestBody @Valid MemberUpdate memberUpdate);
}
