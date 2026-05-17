package com.nomadas.bus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.dto.DriverResponse;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.driver.service.DriverService;
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
class BusControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverService driverService;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        busRepository.deleteAll();
        driverRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void postBuses_shouldReturn201AndBusResponse() throws Exception {
        DriverResponse driver = saveDriver();
        BusCreateRequest request = new BusCreateRequest("1234-ABC", 55, 55, driver.id());

        ResponseEntity<String> response = postJson("/api/buses", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").isNumber()).isTrue();
        assertThat(body.get("plateNumber").asText()).isEqualTo("1234-ABC");
        assertThat(body.get("driverId").asLong()).isEqualTo(driver.id());
        assertThat(body.get("driverFullName").asText()).isEqualTo("Manuel Lopez");
    }

    @Test
    void postBuses_shouldReturn404_whenDriverDoesNotExist() throws Exception {
        BusCreateRequest request = new BusCreateRequest("1234-ABC", 55, 55, 999L);

        ResponseEntity<String> response = postJson("/api/buses", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).isEqualTo("Driver not found");
    }

    private DriverResponse saveDriver() {
        return driverService.create(new DriverCreateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        ));
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
