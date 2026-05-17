package com.nomadas.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.testconfig.JacksonTestConfig;
import com.nomadas.testsupport.InternalAuthSupport;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ContractTest {

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
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void usersCreation_shouldReturnErrorContract_whenBodyIsInvalid() throws Exception {
        String invalidBody = """
                {
                  "firstName": "",
                  "lastName": "Navarro",
                  "dni": "123",
                  "email": "bad-email",
                  "phone": "1",
                  "birthDate": null
                }
                """;

        ResponseEntity<String> response = postJson("/api/users", invalidBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 400, "Bad Request", "Invalid user data", "/api/users");
    }

    @Test
    void protectedUsersEndpoint_shouldReturn401_whenTokenIsMissing() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/users")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 401, "Unauthorized", "Authentication is required", "/api/users");
    }

    @Test
    void login_shouldReturnJwtContract_whenCredentialsAreValid() throws Exception {
        String validBody = """
                {
                  "username": "admin",
                  "password": "admin12345"
                }
                """;

        ResponseEntity<String> response = postJson("/api/auth/login", validBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(fieldNames(body)).containsExactly("token", "type", "credentialId", "username", "email", "role", "expiresAt");
        assertThat(body.get("token").asText()).isNotBlank();
        assertThat(body.get("type").asText()).isEqualTo("Bearer");
        assertThat(body.get("username").asText()).isEqualTo("admin");
        assertThat(body.get("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void publicCatalogEndpoint_shouldReturnJson_withoutAuthentication() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/hotels")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/json");
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
    }

    private ResponseEntity<String> postJson(String path, String body) {
        return restClient()
                .post()
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    private void assertErrorContract(JsonNode body, int status, String error, String message, String path) {
        assertThat(fieldNames(body)).containsExactly("status", "error", "message", "path", "timestamp");
        assertThat(body.get("status").asInt()).isEqualTo(status);
        assertThat(body.get("error").asText()).isEqualTo(error);
        assertThat(body.get("message").asText()).isEqualTo(message);
        assertThat(body.get("path").asText()).isEqualTo(path);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    private List<String> fieldNames(JsonNode node) {
        Set<String> fieldNames = new LinkedHashSet<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        return fieldNames.stream().toList();
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
