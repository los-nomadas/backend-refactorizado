package com.nomadas.user;

import com.nomadas.auth.repository.InternalCredentialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.nomadas.testconfig.JacksonTestConfig;
import com.nomadas.testsupport.InternalAuthSupport;
import com.nomadas.user.dto.UserCreateRequest;
import com.nomadas.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void postUsers_shouldReturn201AndUserResponse() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "David",
                "Navarro",
                "12345678A",
                "david@example.com",
                "600123123",
                LocalDate.parse("1990-04-15")
        );

        ResponseEntity<String> response = postJson("/api/users", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").isNumber()).isTrue();
        assertThat(body.get("firstName").asText()).isEqualTo("David");
        assertThat(body.get("lastName").asText()).isEqualTo("Navarro");
        assertThat(body.get("dni").asText()).isEqualTo("12345678A");
        assertThat(body.get("email").asText()).isEqualTo("david@example.com");
        assertThat(body.get("phone").asText()).isEqualTo("600123123");
        assertThat(body.get("birthDate").asText()).isEqualTo("1990-04-15");
    }

    @Test
    void postUsers_shouldReturn400WithApiError_whenInvalidBody() throws Exception {
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
        assertThat(body.get("status").asInt()).isEqualTo(400);
        assertThat(body.get("error").asText()).isEqualTo("Bad Request");
        assertThat(body.get("message").asText()).isEqualTo("Invalid user data");
        assertThat(body.get("path").asText()).isEqualTo("/api/users");
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void postUsers_shouldReturn409WithApiError_whenDuplicateEmail() throws Exception {
        UserCreateRequest first = new UserCreateRequest(
                "David",
                "Navarro",
                "12345678A",
                "david@example.com",
                "600123123",
                LocalDate.parse("1990-04-15")
        );
        UserCreateRequest second = new UserCreateRequest(
                "Ana",
                "Garcia",
                "87654321B",
                "david@example.com",
                "600000000",
                LocalDate.parse("1995-01-01")
        );

        ResponseEntity<String> firstResponse = postJson("/api/users", objectMapper.writeValueAsString(first));
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> response = postJson("/api/users", objectMapper.writeValueAsString(second));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(409);
        assertThat(body.get("error").asText()).isEqualTo("Conflict");
        assertThat(body.get("message").asText()).isEqualTo("Email already exists");
        assertThat(body.get("path").asText()).isEqualTo("/api/users");
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void getUser_shouldReturn404WithApiError_whenNotFound() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/users/999")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(404);
        assertThat(body.get("error").asText()).isEqualTo("Not Found");
        assertThat(body.get("message").asText()).isEqualTo("User not found");
        assertThat(body.get("path").asText()).isEqualTo("/api/users/999");
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void deleteUser_shouldReturn404_whenNotFound() throws Exception {
        ResponseEntity<String> response = restClient()
                .method(HttpMethod.DELETE)
                .uri("/api/users/999")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).isEqualTo("User not found");
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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }

    private String authToken() {
        try {
            return InternalAuthSupport.login(restClient(), objectMapper);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
