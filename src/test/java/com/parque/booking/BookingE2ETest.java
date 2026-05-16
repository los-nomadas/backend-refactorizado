package com.parque.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parque.auth.repository.InternalCredentialRepository;
import com.parque.booking.dto.BookingCreateRequest;
import com.parque.booking.dto.CompanionRequest;
import com.parque.booking.model.Booking;
import com.parque.booking.repository.BookingRepository;
import com.parque.booking.service.notification.NotificationService;
import com.parque.hotel.model.Hotel;
import com.parque.hotel.repository.HotelRepository;
import com.parque.testconfig.JacksonTestConfig;
import com.parque.testsupport.InternalAuthSupport;
import com.parque.user.dto.UserCreateRequest;
import com.parque.user.repository.UserRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingE2ETest {

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
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
        when(notificationService.sendBookingConfirmation(anyList(), any(Booking.class))).thenReturn(true);
    }

    @Test
    void shouldCreateUserAndBooking_thenExposeBookingToInternalDashboard() throws Exception {
        Hotel hotel = hotelRepository.save(Hotel.builder()
                .name("Hotel Magic Park")
                .description("Hotel familiar situado junto al parque.")
                .location("Granada")
                .totalRooms(120)
                .availableRooms(120)
                .totalPlaces(240)
                .availablePlaces(240)
                .halfBoardPrice(new BigDecimal("80.00"))
                .fullBoardPrice(new BigDecimal("120.00"))
                .imageUrl("https://example.com/hotel.jpg")
                .build());

        UserCreateRequest userRequest = new UserCreateRequest(
                "Juan",
                "Perez",
                "12345678A",
                "juan@example.com",
                "666123456",
                LocalDate.parse("1994-05-18")
        );

        ResponseEntity<String> createdUser = postJson("/api/users", objectMapper.writeValueAsString(userRequest));
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long userId = objectMapper.readTree(createdUser.getBody()).get("id").asLong();

        BookingCreateRequest bookingRequest = new BookingCreateRequest(
                userId,
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Juan", "Perez", LocalDate.parse("1994-05-18")),
                        new CompanionRequest("Nora", "Perez", LocalDate.parse("2016-08-08"))
                )
        );

        ResponseEntity<String> createdBooking = postJson("/api/bookings", objectMapper.writeValueAsString(bookingRequest));
        assertThat(createdBooking.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode createdBookingBody = objectMapper.readTree(createdBooking.getBody());
        assertThat(createdBookingBody.get("emailSent").asBoolean()).isTrue();
        long bookingId = createdBookingBody.get("id").asLong();

        ResponseEntity<String> bookingDetail = restClient()
                .get()
                .uri("/api/bookings/" + bookingId)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(bookingDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode bookingDetailBody = objectMapper.readTree(bookingDetail.getBody());
        assertThat(bookingDetailBody.get("userFullName").asText()).isEqualTo("Juan Perez");
        assertThat(bookingDetailBody.get("hotelName").asText()).isEqualTo("Hotel Magic Park");
        assertThat(bookingDetailBody.get("tickets")).hasSize(2);
    }

    @Test
    void internalBookingsEndpoint_shouldRejectMissingToken() throws Exception {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/bookings")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void bookingCreation_shouldRejectMinorWithoutAdult() throws Exception {
        UserCreateRequest userRequest = new UserCreateRequest(
                "Pedro",
                "Lopez",
                "87654321B",
                "pedro@example.com",
                "666654321",
                LocalDate.parse("1988-01-01")
        );

        ResponseEntity<String> createdUser = postJson("/api/users", objectMapper.writeValueAsString(userRequest));
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long userId = objectMapper.readTree(createdUser.getBody()).get("id").asLong();

        BookingCreateRequest bookingRequest = new BookingCreateRequest(
                userId,
                null,
                null,
                "HALF_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Lucas", "Lopez", LocalDate.parse("2018-03-10")))
        );

        ResponseEntity<String> createdBooking = postJson("/api/bookings", objectMapper.writeValueAsString(bookingRequest));
        assertThat(createdBooking.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode errorBody = objectMapper.readTree(createdBooking.getBody());
        assertThat(errorBody.get("message").asText()).isEqualTo("A minor cannot travel without an adult");
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
