package com.parque.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parque.auth.repository.InternalCredentialRepository;
import com.parque.testconfig.JacksonTestConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.parque.testsupport.InternalAuthSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        internalCredentialRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void postLogin_shouldReturn200AndLoginResponse() throws Exception {
        String body = """
                {
                  "username": "admin",
                  "password": "admin12345"
                }
                """;

        ResponseEntity<String> response = postJson("/api/auth/login", body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("token").asText()).isNotBlank();
        assertThat(json.get("type").asText()).isEqualTo("Bearer");
        assertThat(json.get("credentialId").asLong()).isPositive();
        assertThat(json.get("username").asText()).isEqualTo("admin");
        assertThat(json.get("email").asText()).isEqualTo("admin@nomadas.local");
        assertThat(json.get("role").asText()).isEqualTo("ADMIN");
        assertThat(json.get("expiresAt").asText()).isNotBlank();
    }

    @Test
    void postLogin_shouldReturn400WithApiError_whenInvalidBody() throws Exception {
        String body = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        ResponseEntity<String> response = postJson("/api/auth/login", body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("status").asInt()).isEqualTo(400);
        assertThat(json.get("error").asText()).isEqualTo("Bad Request");
        assertThat(json.get("message").asText()).isEqualTo("Invalid login data");
        assertThat(json.get("path").asText()).isEqualTo("/api/auth/login");
        assertThat(json.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void postLogin_shouldReturn401WithApiError_whenCredentialsAreInvalid() throws Exception {
        String body = """
                {
                  "username": "admin",
                  "password": "wrong-password"
                }
                """;

        ResponseEntity<String> response = postJson("/api/auth/login", body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("status").asInt()).isEqualTo(401);
        assertThat(json.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(json.get("message").asText()).isEqualTo("Invalid credentials");
        assertThat(json.get("path").asText()).isEqualTo("/api/auth/login");
        assertThat(json.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void protectedRoute_shouldReturn401_whenTokenIsMissing() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/users")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("status").asInt()).isEqualTo(401);
        assertThat(json.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(json.get("message").asText()).isEqualTo("Authentication is required");
    }

    private ResponseEntity<String> postJson(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return restClient()
                .post()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
