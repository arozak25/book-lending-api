package dev.arozaakk.booklendingapi.controller;

import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberCreate;
import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberUpdate;
import static dev.arozaakk.booklendingapi.testsupport.SqlScriptPaths.CLEANUP_SQL;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.arozaakk.booklendingapi.controller.resource.MemberResource;
import dev.arozaakk.booklendingapi.model.Member;
import dev.arozaakk.booklendingapi.model.MemberCreate;
import dev.arozaakk.booklendingapi.model.MemberUpdate;
import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
import dev.arozaakk.booklendingapi.service.MemberService;
import jakarta.inject.Inject;
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
public class MemberControllerIntegrationTest {
  @Inject private MockMvc mockMvc;
  @Inject private MemberService memberService;
  @Inject private ObjectMapper objectMapper;

  @Test
  void createMember_withoutUser_shouldReturnUnauthorized() throws Exception {
    MemberCreate memberCreate = createMemberCreate();

    mockMvc
        .perform(
            post(MemberResource.PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberCreate)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getMemberById_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            get(MemberResource.PATH + "/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void findMembers_withoutUser_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get(MemberResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateMember_withoutUser_shouldReturnUnauthorized() throws Exception {
    MemberUpdate memberUpdate = createMemberUpdate();

    mockMvc
        .perform(
            put(MemberResource.PATH + "/{id}", UUID.randomUUID())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdate)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createMember_withAdmin_shouldReturnCreatedMember() throws Exception {
    MemberCreate memberCreate = createMemberCreate();

    mockMvc
        .perform(
            post(MemberResource.PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberCreate)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isString())
        .andExpect(jsonPath("$.name").value(memberCreate.name()))
        .andExpect(jsonPath("$.email").value(memberCreate.email()))
        .andExpect(jsonPath("$.status").value(MemberStatus.ACTIVE.name()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getMemberById_withAdmin_shouldReturnMember() throws Exception {
    Member member = memberService.createMember(createMemberCreate());
    UUID id = member.id();

    mockMvc
        .perform(get(MemberResource.PATH + "/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value(member.name()))
        .andExpect(jsonPath("$.email").value(member.email()))
        .andExpect(jsonPath("$.status").value(member.status().name()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void findMembers_withAdmin_shouldReturnMembers() throws Exception {
    Member firstMember =
        memberService.createMember(createMemberCreate("John Doe", "john.doe@example.com"));
    Member secondMember =
        memberService.createMember(createMemberCreate("Jane Doe", "jane.doe@example.com"));

    mockMvc
        .perform(get(MemberResource.PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(
            jsonPath(
                "$[*].id", hasItems(firstMember.id().toString(), secondMember.id().toString())));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateMember_withAdmin_shouldReturnUpdatedMember() throws Exception {
    Member createdMember = memberService.createMember(createMemberCreate());
    UUID id = createdMember.id();
    MemberUpdate memberUpdate = createMemberUpdate();

    mockMvc
        .perform(
            put(MemberResource.PATH + "/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdate)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value(memberUpdate.name()))
        .andExpect(jsonPath("$.email").value(memberUpdate.email()))
        .andExpect(jsonPath("$.status").value(memberUpdate.status().name()));
  }
}
