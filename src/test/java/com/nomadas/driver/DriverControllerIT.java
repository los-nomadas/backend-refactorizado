package com.nomadas.driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.repository.DriverRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DriverControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void postDrivers_shouldReturn201AndDriverResponse() throws Exception {
        DriverCreateRequest request = new DriverCreateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        );

        ResponseEntity<String> response = postJson("/api/drivers", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").isNumber()).isTrue();
        assertThat(body.get("firstName").asText()).isEqualTo("Manuel");
        assertThat(body.get("licenseNumber").asText()).isEqualTo("A-12345678");
        assertThat(body.get("available").asBoolean()).isTrue();
    }

    @Test
    void postDrivers_shouldReturn409_whenDniExists() throws Exception {
        DriverCreateRequest first = new DriverCreateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        );
        DriverCreateRequest second = new DriverCreateRequest(
                "Ana",
                "Garcia",
                "98765432B",
                "B-87654321",
                "612345679",
                "ana.driver@example.com",
                true
        );

        assertThat(postJson("/api/drivers", objectMapper.writeValueAsString(first)).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ResponseEntity<String> response = postJson("/api/drivers", objectMapper.writeValueAsString(second));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).isEqualTo("DNI already exists");
    }

    private ResponseEntity<String> postJson(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.setBearerAuth(authToken());
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

    private String authToken() {
        try {
            return InternalAuthSupport.login(restClient(), objectMapper);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
