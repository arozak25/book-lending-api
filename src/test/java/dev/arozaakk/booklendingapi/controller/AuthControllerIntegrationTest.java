package dev.arozaakk.booklendingapi.controller;

import static dev.arozaakk.booklendingapi.common.SqlScriptPaths.CLEANUP_SQL;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.arozaakk.booklendingapi.controller.resource.BookResource;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("integration-test")
@SqlGroup({@Sql(scripts = CLEANUP_SQL, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)})
class AuthControllerIntegrationTest {
  @Inject private WebApplicationContext context;
  @Inject private ObjectMapper objectMapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void createToken_withValidCredentials_shouldReturnBearerToken() throws Exception {
    mockMvc
        .perform(
            post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("username", "admin", "password", "Admin123!"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @Test
  void createToken_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("username", "admin", "password", "wrong-password"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void findBooks_withValidJwt_shouldReturnOk() throws Exception {
    String token = generateToken();

    mockMvc
        .perform(
            get(BookResource.PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void actuatorHealth_withoutAuthentication_shouldReturnOk() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  private String generateToken() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("username", "admin", "password", "Admin123!"))))
            .andExpect(status().isOk())
            .andReturn();

    Map<String, String> payload =
        objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
    return payload.get("token");
  }
}
