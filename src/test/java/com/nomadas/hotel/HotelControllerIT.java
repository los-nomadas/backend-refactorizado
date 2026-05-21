package com.nomadas.hotel;

import com.nomadas.auth.repository.InternalCredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.testconfig.JacksonTestConfig;
import com.nomadas.hotel.dto.HotelCreateRequest;
import com.nomadas.hotel.dto.HotelUpdateRequest;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.testsupport.InternalAuthSupport;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HotelControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void postHotels_shouldReturn201AndHotelResponse() throws Exception {
        HotelCreateRequest request = new HotelCreateRequest(
                "Hotel Magic Park",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                120,
                240,
                240,
                new BigDecimal("80.0"),
                new BigDecimal("120.0"),
                "https://example.com/hotel.jpg"
        );

        ResponseEntity<String> response = postJson("/api/hotels", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").isNumber()).isTrue();
        assertThat(body.get("name").asText()).isEqualTo("Hotel Magic Park");
        assertThat(body.get("location").asText()).isEqualTo("Granada");
        assertThat(body.get("totalRooms").asInt()).isEqualTo(120);
        assertThat(body.get("availablePlaces").asInt()).isEqualTo(240);
        assertThat(body.get("imageUrl").asText()).isEqualTo("https://example.com/hotel.jpg");
    }

    @Test
    void getHotels_shouldReturn200AndList() throws Exception {
        HotelCreateRequest request = new HotelCreateRequest(
                "Hotel Magic Park",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                120,
                240,
                240,
                new BigDecimal("80.0"),
                new BigDecimal("120.0"),
                "https://example.com/hotel.jpg"
        );
        ResponseEntity<String> createdResponse = postJson("/api/hotels", objectMapper.writeValueAsString(request));
        assertThat(createdResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/hotels")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isGreaterThanOrEqualTo(1);
        List<String> fields = List.of(
                "id",
                "name",
                "description",
                "location",
                "totalRooms",
                "availableRooms",
                "totalPlaces",
                "availablePlaces",
                "halfBoardPrice",
                "fullBoardPrice",
                "imageUrl"
        );
        for (String field : fields) {
            assertThat(body.get(0).hasNonNull(field)).isTrue();
        }
    }

    @Test
    void putHotels_shouldReturn200AndUpdatedHotel() throws Exception {
        HotelCreateRequest createRequest = new HotelCreateRequest(
                "Hotel Magic Park",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                120,
                240,
                240,
                new BigDecimal("80.0"),
                new BigDecimal("120.0"),
                "https://example.com/hotel.jpg"
        );

        ResponseEntity<String> createdResponse = postJson("/api/hotels", objectMapper.writeValueAsString(createRequest));
        assertThat(createdResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode createdBody = objectMapper.readTree(createdResponse.getBody());
        long id = createdBody.get("id").asLong();

        HotelUpdateRequest updateRequest = new HotelUpdateRequest(
                "Hotel Magic Park Resort",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                80,
                240,
                160,
                new BigDecimal("90.0"),
                new BigDecimal("130.0"),
                "https://example.com/hotel.jpg"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.setBearerAuth(authToken());
        ResponseEntity<String> response = restClient()
                .put()
                .uri("/api/hotels/" + id)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(objectMapper.writeValueAsString(updateRequest))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isEqualTo(id);
        assertThat(body.get("name").asText()).isEqualTo("Hotel Magic Park Resort");
        assertThat(body.get("availableRooms").asInt()).isEqualTo(80);
        assertThat(body.get("availablePlaces").asInt()).isEqualTo(160);
        assertThat(body.get("halfBoardPrice").asDouble()).isEqualTo(90.0);
        assertThat(body.get("fullBoardPrice").asDouble()).isEqualTo(130.0);
    }

    @Test
    void postHotels_shouldReturn400WithApiError_whenInvalidBody() throws Exception {
        String invalidBody = """
                {
                  "name": "",
                  "description": "",
                  "location": "",
                  "totalRooms": 0,
                  "availableRooms": -1,
                  "totalPlaces": 0,
                  "availablePlaces": -1,
                  "halfBoardPrice": 0,
                  "fullBoardPrice": 0,
                  "imageUrl": ""
                }
                """;

        ResponseEntity<String> response = postJson("/api/hotels", invalidBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(400);
        assertThat(body.get("error").asText()).isEqualTo("Bad Request");
        assertThat(body.get("message").asText()).isEqualTo("Invalid hotel data");
        assertThat(body.get("path").asText()).isEqualTo("/api/hotels");
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void getHotel_shouldReturn404WithApiError_whenNotFound() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/hotels/999")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asInt()).isEqualTo(404);
        assertThat(body.get("error").asText()).isEqualTo("Not Found");
        assertThat(body.get("message").asText()).isEqualTo("Hotel not found");
        assertThat(body.get("path").asText()).isEqualTo("/api/hotels/999");
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void deleteHotel_shouldReturn404_whenNotFound() throws Exception {
        ResponseEntity<String> response = restClient()
                .method(HttpMethod.DELETE)
                .uri("/api/hotels/999")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).isEqualTo("Hotel not found");
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
