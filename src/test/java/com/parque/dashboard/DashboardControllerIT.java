package com.parque.dashboard;

import com.parque.auth.repository.InternalCredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parque.booking.dto.BookingCreateRequest;
import com.parque.booking.dto.CompanionRequest;
import com.parque.dashboard.repository.BookingDashboardRepository;
import com.parque.dashboard.repository.TicketRepository;
import com.parque.hotel.model.Hotel;
import com.parque.hotel.repository.HotelRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DashboardControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingDashboardRepository bookingDashboardRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        bookingDashboardRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void endpoints_shouldReturn200WithEmptyMetricsWhenNoData() throws Exception {
        int currentYear = LocalDate.now().getYear();

        ResponseEntity<String> revenue = restClient()
                .get()
                .uri("/api/dashboard/current-year-revenue")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);

        assertThat(revenue.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode revenueBody = objectMapper.readTree(revenue.getBody());
        assertThat(fieldNames(revenueBody)).containsExactly("year", "totalRevenue");
        assertThat(revenueBody.get("year").asInt()).isEqualTo(currentYear);
        assertThat(revenueBody.get("totalRevenue").asDouble()).isEqualTo(0.0);

        ResponseEntity<String> tickets = restClient()
                .get()
                .uri("/api/dashboard/tickets-by-age-range?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(tickets.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode ticketsBody = objectMapper.readTree(tickets.getBody());
        assertThat(ticketsBody.isArray()).isTrue();
        assertThat(ticketsBody).isEmpty();

        ResponseEntity<String> topHotels = restClient()
                .get()
                .uri("/api/dashboard/top-hotels?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(topHotels.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode topHotelsBody = objectMapper.readTree(topHotels.getBody());
        assertThat(topHotelsBody.isArray()).isTrue();
        assertThat(topHotelsBody).isEmpty();

        ResponseEntity<String> summary = restClient()
                .get()
                .uri("/api/dashboard/summary?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode summaryBody = objectMapper.readTree(summary.getBody());
        assertThat(fieldNames(summaryBody)).containsExactly("year", "totalRevenue", "ticketsByAgeRange", "topHotels");
        assertThat(summaryBody.get("year").asInt()).isEqualTo(currentYear);
        assertThat(summaryBody.get("totalRevenue").asDouble()).isEqualTo(0.0);
        assertThat(summaryBody.get("ticketsByAgeRange")).isEmpty();
        assertThat(summaryBody.get("topHotels")).isEmpty();
    }

    @Test
    void endpoints_shouldReturnRealMetricsFromBookingsCreatedThroughApi() throws Exception {
        int currentYear = LocalDate.now().getYear();
        int previousYear = currentYear - 1;

        User user = userRepository.save(User.builder()
                .firstName("David")
                .lastName("Navarro")
                .dni("12345678A")
                .email("david@example.com")
                .phone("600123123")
                .birthDate(LocalDate.parse("1990-04-15"))
                .build());

        Hotel hotelMagicPark = saveHotel(
                "Hotel Magic Park",
                "Hotel familiar situado junto al parque.",
                120,
                240,
                new BigDecimal("80.0"),
                new BigDecimal("120.0"),
                "https://example.com/hotel.jpg"
        );

        Hotel hotelAdventure = saveHotel(
                "Hotel Adventure",
                "Hotel junto al parque.",
                100,
                200,
                new BigDecimal("70.0"),
                new BigDecimal("110.0"),
                "https://example.com/hotel2.jpg"
        );

        Hotel hotelFantasy = saveHotel(
                "Hotel Fantasy",
                "Hotel tematico.",
                90,
                180,
                new BigDecimal("60.0"),
                new BigDecimal("90.0"),
                "https://example.com/hotel3.jpg"
        );

        Hotel hotelOcean = saveHotel(
                "Hotel Ocean",
                "Hotel acuatico.",
                80,
                160,
                new BigDecimal("55.0"),
                new BigDecimal("85.0"),
                "https://example.com/hotel4.jpg"
        );

        long bookingOneId = createBookingThroughApi(
                user.getId(),
                hotelMagicPark.getId(),
                "FULL_BOARD",
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20")),
                        new CompanionRequest("Maria", "Perez", LocalDate.parse("1950-08-01"))
                )
        );
        updateBookingCreatedAt(bookingOneId, LocalDateTime.of(currentYear, 5, 1, 10, 0, 0));

        long bookingTwoId = createBookingThroughApi(
                user.getId(),
                hotelMagicPark.getId(),
                "HALF_BOARD",
                List.of(
                        new CompanionRequest("Pedro", "Lopez", LocalDate.parse("1987-01-10")),
                        new CompanionRequest("Laura", "Lopez", LocalDate.parse("1991-02-15"))
                )
        );
        updateBookingCreatedAt(bookingTwoId, LocalDateTime.of(currentYear, 6, 1, 10, 0, 0));

        long bookingThreeId = createBookingThroughApi(
                user.getId(),
                hotelAdventure.getId(),
                "HALF_BOARD",
                List.of(
                        new CompanionRequest("Nina", "Ruiz", LocalDate.parse("2012-04-18")),
                        new CompanionRequest("Jorge", "Ruiz", LocalDate.parse("1986-11-05"))
                )
        );
        updateBookingCreatedAt(bookingThreeId, LocalDateTime.of(currentYear, 7, 1, 10, 0, 0));

        long bookingFourId = createBookingThroughApi(
                user.getId(),
                hotelFantasy.getId(),
                "HALF_BOARD",
                List.of(new CompanionRequest("Pablo", "Sanchez", LocalDate.parse("1975-01-01")))
        );
        updateBookingCreatedAt(bookingFourId, LocalDateTime.of(currentYear, 8, 1, 10, 0, 0));

        long bookingFiveId = createBookingThroughApi(
                user.getId(),
                hotelOcean.getId(),
                "HALF_BOARD",
                List.of(new CompanionRequest("Elena", "Martin", LocalDate.parse("1989-06-06")))
        );
        updateBookingCreatedAt(bookingFiveId, LocalDateTime.of(currentYear, 9, 1, 10, 0, 0));

        long bookingSixId = createBookingThroughApi(
                user.getId(),
                null,
                "HALF_BOARD",
                List.of(new CompanionRequest("Mario", "Gil", LocalDate.parse("1993-03-03")))
        );
        updateBookingCreatedAt(bookingSixId, LocalDateTime.of(currentYear, 10, 1, 10, 0, 0));

        long previousYearBookingId = createBookingThroughApi(
                user.getId(),
                hotelAdventure.getId(),
                "FULL_BOARD",
                List.of(new CompanionRequest("Old", "Ticket", LocalDate.parse("1980-07-07")))
        );
        updateBookingCreatedAt(previousYearBookingId, LocalDateTime.of(previousYear, 7, 1, 10, 0, 0));

        ResponseEntity<String> tickets = restClient()
                .get()
                .uri("/api/dashboard/tickets-by-age-range?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(tickets.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode ticketsBody = objectMapper.readTree(tickets.getBody());
        assertThat(ticketsBody.isArray()).isTrue();
        assertThat(ticketsBody).hasSize(3);
        assertThat(fieldNames(ticketsBody.get(0))).containsExactly("ageRange", "ticketsSold");
        assertThat(ticketsBody.get(0).get("ageRange").asText()).isEqualTo("CHILD");
        assertThat(ticketsBody.get(0).get("ticketsSold").asLong()).isEqualTo(2);
        assertThat(ticketsBody.get(1).get("ageRange").asText()).isEqualTo("ADULT");
        assertThat(ticketsBody.get(1).get("ticketsSold").asLong()).isEqualTo(7);
        assertThat(ticketsBody.get(2).get("ageRange").asText()).isEqualTo("SENIOR");
        assertThat(ticketsBody.get(2).get("ticketsSold").asLong()).isEqualTo(1);

        ResponseEntity<String> revenue = restClient()
                .get()
                .uri("/api/dashboard/current-year-revenue")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(revenue.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode revenueBody = objectMapper.readTree(revenue.getBody());
        assertThat(revenueBody.get("year").asInt()).isEqualTo(currentYear);
        assertThat(revenueBody.get("totalRevenue").asDouble()).isEqualTo(825.0);

        ResponseEntity<String> topHotels = restClient()
                .get()
                .uri("/api/dashboard/top-hotels?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(topHotels.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode topHotelsBody = objectMapper.readTree(topHotels.getBody());
        assertThat(topHotelsBody.isArray()).isTrue();
        assertThat(topHotelsBody).hasSize(3);
        assertThat(fieldNames(topHotelsBody.get(0))).containsExactly("hotelId", "hotelName", "revenue");
        assertThat(topHotelsBody.get(0).get("hotelId").asLong()).isEqualTo(hotelMagicPark.getId());
        assertThat(topHotelsBody.get(0).get("hotelName").asText()).isEqualTo("Hotel Magic Park");
        assertThat(topHotelsBody.get(0).get("revenue").asDouble()).isEqualTo(440.0);
        assertThat(topHotelsBody.get(1).get("hotelId").asLong()).isEqualTo(hotelAdventure.getId());
        assertThat(topHotelsBody.get(1).get("hotelName").asText()).isEqualTo("Hotel Adventure");
        assertThat(topHotelsBody.get(1).get("revenue").asDouble()).isEqualTo(135.0);
        assertThat(topHotelsBody.get(2).get("hotelId").asLong()).isEqualTo(hotelFantasy.getId());
        assertThat(topHotelsBody.get(2).get("hotelName").asText()).isEqualTo("Hotel Fantasy");
        assertThat(topHotelsBody.get(2).get("revenue").asDouble()).isEqualTo(105.0);

        ResponseEntity<String> summary = restClient()
                .get()
                .uri("/api/dashboard/summary?year=" + currentYear)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
                .retrieve()
                .toEntity(String.class);
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode summaryBody = objectMapper.readTree(summary.getBody());
        assertThat(fieldNames(summaryBody)).containsExactly("year", "totalRevenue", "ticketsByAgeRange", "topHotels");
        assertThat(summaryBody.get("year").asInt()).isEqualTo(currentYear);
        assertThat(summaryBody.get("totalRevenue").asDouble()).isEqualTo(825.0);
        assertThat(summaryBody.get("ticketsByAgeRange")).hasSize(3);
        assertThat(summaryBody.get("topHotels")).hasSize(3);
    }

    private Hotel saveHotel(
            String name,
            String description,
            int totalRooms,
            int totalPlaces,
            BigDecimal halfBoardPrice,
            BigDecimal fullBoardPrice,
            String imageUrl
    ) {
        return hotelRepository.save(Hotel.builder()
                .name(name)
                .description(description)
                .location("Granada")
                .totalRooms(totalRooms)
                .availableRooms(totalRooms)
                .totalPlaces(totalPlaces)
                .availablePlaces(totalPlaces)
                .halfBoardPrice(halfBoardPrice)
                .fullBoardPrice(fullBoardPrice)
                .imageUrl(imageUrl)
                .build());
    }

    private long createBookingThroughApi(
            Long userId,
            Long hotelId,
            String boardType,
            List<CompanionRequest> companions
    ) throws Exception {
        BookingCreateRequest request = new BookingCreateRequest(
                userId,
                null,
                hotelId,
                boardType,
                LocalDate.parse("2026-05-22"),
                companions
        );

        ResponseEntity<String> response = postJson("/api/bookings", objectMapper.writeValueAsString(request));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return objectMapper.readTree(response.getBody()).get("id").asLong();
    }

    private void updateBookingCreatedAt(long bookingId, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "update bookings set created_at = ? where id = ?",
                Timestamp.valueOf(createdAt),
                bookingId
        );
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

    private String authToken() {
        try {
            return InternalAuthSupport.login(restClient(), objectMapper);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
