package com.parque.booking;

import com.parque.auth.repository.InternalCredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parque.booking.dto.BookingCreateRequest;
import com.parque.booking.dto.CompanionRequest;
import com.parque.booking.model.Booking;
import com.parque.booking.repository.BookingRepository;
import com.parque.booking.service.notification.NotificationService;
import com.parque.hotel.model.Hotel;
import com.parque.hotel.repository.HotelRepository;
import com.parque.offer.repository.OfferRepository;
import com.parque.testconfig.JacksonTestConfig;
import com.parque.testsupport.InternalAuthSupport;
import com.parque.user.model.User;
import com.parque.user.repository.UserRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        offerRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
        when(notificationService.sendBookingConfirmation(anyList(), any(Booking.class))).thenReturn(true);
    }

    @Test
    void postBookings_shouldReturn201AndBookingResponse() throws Exception {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(4);

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/json");
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(fieldNames(body)).containsExactly(
                "id",
                "userId",
                "userFullName",
                "hotelId",
                "hotelName",
                "boardType",
                "visitDate",
                "tickets",
                "totalPrice",
                "emailSent",
                "createdAt"
        );
        assertThat(body.get("id").isNumber()).isTrue();
        assertThat(body.get("userId").asLong()).isEqualTo(user.getId());
        assertThat(body.get("userFullName").asText()).isEqualTo("David Navarro");
        assertThat(body.get("hotelId").asLong()).isEqualTo(hotel.getId());
        assertThat(body.get("hotelName").asText()).isEqualTo("Hotel Magic Park");
        assertThat(body.get("boardType").asText()).isEqualTo("FULL_BOARD");
        assertThat(body.get("visitDate").asText()).isEqualTo("2026-05-22");
        assertThat(body.get("tickets").isArray()).isTrue();
        assertThat(body.get("tickets").size()).isEqualTo(2);
        assertThat(fieldNames(body.get("tickets").get(0))).containsExactly("holderFullName", "ageRange", "price");
        assertThat(fieldNames(body.get("tickets").get(1))).containsExactly("holderFullName", "ageRange", "price");
        assertThat(body.get("tickets").get(0).get("holderFullName").asText()).isEqualTo("Ana Garcia");
        assertThat(body.get("tickets").get(0).get("ageRange").asText()).isEqualTo("ADULT");
        assertThat(body.get("tickets").get(0).get("price").asDouble()).isEqualTo(65.0);
        assertThat(body.get("tickets").get(1).get("holderFullName").asText()).isEqualTo("Lucas Garcia");
        assertThat(body.get("tickets").get(1).get("ageRange").asText()).isEqualTo("CHILD");
        assertThat(body.get("tickets").get(1).get("price").asDouble()).isEqualTo(30.0);
        assertThat(body.get("totalPrice").asDouble()).isEqualTo(215.0);
        assertThat(body.get("emailSent").asBoolean()).isTrue();
        assertThat(body.get("createdAt").asText()).isNotBlank();
    }

    @Test
    void postBookings_shouldReturn400WithApiError_whenInvalidBody() throws Exception {
        User user = saveUser("david@example.com", "12345678A");

        String invalidBody = """
                {
                  "userId": %d,
                  "offerId": null,
                  "hotelId": null,
                  "boardType": "",
                  "visitDate": null,
                  "companions": []
                }
                """.formatted(user.getId());

        ResponseEntity<String> response = postJson("/api/bookings", invalidBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 400, "Bad Request", "Invalid booking data", "/api/bookings");
    }

    @Test
    void postBookings_shouldReturn400WithApiError_whenBoardTypeIsInvalid() throws Exception {
        User user = saveUser("david@example.com", "12345678A");

        String invalidBody = """
                {
                  "userId": %d,
                  "offerId": null,
                  "hotelId": null,
                  "boardType": "ROOM_ONLY",
                  "visitDate": "2026-05-22",
                  "companions": [
                    {
                      "firstName": "Ana",
                      "lastName": "Garcia",
                      "birthDate": "1988-03-10"
                    }
                  ]
                }
                """.formatted(user.getId());

        ResponseEntity<String> response = postJson("/api/bookings", invalidBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 400, "Bad Request", "Invalid booking data", "/api/bookings");
    }

    @Test
    void postBookings_shouldReturn409WithApiError_whenHotelIsFull() throws Exception {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(1);

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 409, "Conflict", "Hotel is full", "/api/bookings");
    }

    @Test
    void postBookings_shouldReturn409WithApiError_whenMinorTravelsWithoutAdult() throws Exception {
        User user = saveUser("david@example.com", "12345678A");

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                null,
                "HALF_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20")))
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 409, "Conflict", "A minor cannot travel without an adult", "/api/bookings");
    }

    @Test
    void postBookings_shouldReturn404WithApiError_whenUserNotFound() throws Exception {
        Hotel hotel = saveHotel(4);

        BookingCreateRequest request = new BookingCreateRequest(
                999L,
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")))
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 404, "Not Found", "User not found", "/api/bookings");
    }

    @Test
    void postBookings_shouldReturn404WithApiError_whenHotelNotFound() throws Exception {
        User user = saveUser("david@example.com", "12345678A");

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                999L,
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")))
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 404, "Not Found", "Hotel not found", "/api/bookings");
    }

    @Test
    void getBookings_shouldReturn200AndSummaryList() throws Exception {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(4);

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        );
        ResponseEntity<String> created = postJson("/api/bookings", objectMapper.writeValueAsString(request));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/bookings")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/json");
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body).hasSize(1);
        assertThat(fieldNames(body.get(0))).containsExactly(
                "id",
                "userFullName",
                "hotelName",
                "visitDate",
                "totalTickets",
                "totalPrice",
                "createdAt"
        );
        assertThat(body.get(0).get("userFullName").asText()).isEqualTo("David Navarro");
        assertThat(body.get(0).get("hotelName").asText()).isEqualTo("Hotel Magic Park");
        assertThat(body.get(0).get("visitDate").asText()).isEqualTo("2026-05-22");
        assertThat(body.get(0).get("totalTickets").asInt()).isEqualTo(2);
        assertThat(body.get(0).get("totalPrice").asDouble()).isEqualTo(215.0);
        assertThat(body.get(0).get("createdAt").asText()).isNotBlank();
    }

    @Test
    void getBooking_shouldReturn200AndBookingDetail() throws Exception {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(4);

        BookingCreateRequest request = new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        );
        ResponseEntity<String> created = postJson("/api/bookings", objectMapper.writeValueAsString(request));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long bookingId = objectMapper.readTree(created.getBody()).get("id").asLong();

        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/bookings/" + bookingId)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/json");
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(fieldNames(body)).containsExactly(
                "id",
                "userId",
                "userFullName",
                "hotelId",
                "hotelName",
                "boardType",
                "visitDate",
                "tickets",
                "totalPrice",
                "emailSent",
                "createdAt"
        );
        assertThat(body.get("id").asLong()).isEqualTo(bookingId);
        assertThat(body.get("userId").asLong()).isEqualTo(user.getId());
        assertThat(body.get("userFullName").asText()).isEqualTo("David Navarro");
        assertThat(body.get("hotelId").asLong()).isEqualTo(hotel.getId());
        assertThat(body.get("hotelName").asText()).isEqualTo("Hotel Magic Park");
        assertThat(body.get("boardType").asText()).isEqualTo("FULL_BOARD");
        assertThat(body.get("visitDate").asText()).isEqualTo("2026-05-22");
        assertThat(body.get("tickets").isArray()).isTrue();
        assertThat(body.get("tickets").size()).isEqualTo(2);
        assertThat(fieldNames(body.get("tickets").get(0))).containsExactly("holderFullName", "ageRange", "price");
        assertThat(fieldNames(body.get("tickets").get(1))).containsExactly("holderFullName", "ageRange", "price");
        assertThat(body.get("totalPrice").asDouble()).isEqualTo(215.0);
        assertThat(body.get("emailSent").asBoolean()).isTrue();
        assertThat(body.get("createdAt").asText()).isNotBlank();
    }

    @Test
    void getBooking_shouldReturn404WithApiError_whenNotFound() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/bookings/999")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertErrorContract(body, 404, "Not Found", "Booking not found", "/api/bookings/999");
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

    private String authToken() {
        try {
            return InternalAuthSupport.login(restClient(), objectMapper);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
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

    private User saveUser(String email, String dni) {
        return userRepository.save(User.builder()
                .firstName("David")
                .lastName("Navarro")
                .dni(dni)
                .email(email)
                .phone("600123123")
                .birthDate(LocalDate.parse("1990-04-15"))
                .build());
    }

    private Hotel saveHotel(int availablePlaces) {
        return hotelRepository.save(Hotel.builder()
                .name("Hotel Magic Park")
                .description("Hotel familiar situado junto al parque.")
                .location("Granada")
                .totalRooms(120)
                .availableRooms(120)
                .totalPlaces(240)
                .availablePlaces(availablePlaces)
                .halfBoardPrice(new BigDecimal("80.00"))
                .fullBoardPrice(new BigDecimal("120.00"))
                .imageUrl("https://example.com/hotel.jpg")
                .build());
    }
}
