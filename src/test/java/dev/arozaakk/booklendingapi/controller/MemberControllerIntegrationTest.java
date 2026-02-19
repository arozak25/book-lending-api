package dev.arozaakk.booklendingapi.controller;

import static dev.arozaakk.booklendingapi.common.RestDocs.prettyDocument;
import static dev.arozaakk.booklendingapi.common.SqlScriptPaths.CLEANUP_SQL;
import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberCreate;
import static dev.arozaakk.booklendingapi.factory.MemberFactory.createMemberUpdate;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
public class MemberControllerIntegrationTest {
  @Inject private WebApplicationContext context;
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
  void createMember_withoutUser_shouldReturnUnauthorized() throws Exception {
    MemberCreate memberCreate = createMemberCreate();

    mockMvc
        .perform(
            post(MemberResource.PATH)
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberCreate)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isString())
        .andExpect(jsonPath("$.name").value(memberCreate.name()))
        .andExpect(jsonPath("$.email").value(memberCreate.email()))
        .andExpect(jsonPath("$.status").value(MemberStatus.ACTIVE.name()))
        .andDo(prettyDocument("members-create"));
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
        .andExpect(jsonPath("$.status").value(member.status().name()))
        .andDo(prettyDocument("members-get-by-id"));
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
                "$[*].id", hasItems(firstMember.id().toString(), secondMember.id().toString())))
        .andDo(prettyDocument("members-list"));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberUpdate)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value(memberUpdate.name()))
        .andExpect(jsonPath("$.email").value(memberUpdate.email()))
        .andExpect(jsonPath("$.status").value(memberUpdate.status().name()))
        .andDo(prettyDocument("members-update"));
  }
}
