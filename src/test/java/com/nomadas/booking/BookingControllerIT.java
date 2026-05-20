package com.nomadas.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.entity.InternalCredential;
import com.nomadas.testconfig.JacksonTestConfig;
import com.nomadas.testsupport.DomainFixtures;
import com.nomadas.testsupport.InternalAuthSupport;
import com.nomadas.user.model.User;
import com.nomadas.user.repository.UserRepository;
import com.nomadas.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
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
class BookingControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        internalCredentialRepository.deleteAll();
        userRepository.deleteAll();
        InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
    }

    @Test
    void getMyBookings_shouldReturn404_whenCredentialHasNoLinkedCustomer() throws Exception {
        ResponseEntity<String> response = getAuthorized("/api/bookings/my");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("message").asText()).isEqualTo("No customer linked to this account");
    }

    @Test
    void getMyBookings_shouldReturn200AndEmptyList_whenLinkedCustomerHasNoBookings() throws Exception {
        User customer = userRepository.findById(
                DomainFixtures.user(userService, "12345678A", "carla@example.com").id()
        ).orElseThrow();
        InternalCredential admin = internalCredentialRepository
                .findByUsername(InternalAuthSupport.ADMIN_USERNAME).orElseThrow();
        admin.setUser(customer);
        internalCredentialRepository.save(admin);

        ResponseEntity<String> response = getAuthorized("/api/bookings/my");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body).isEmpty();
    }

    @Test
    void getMyBookings_shouldReturn401_whenTokenIsMissing() {
        ResponseEntity<String> response = restClient()
                .get()
                .uri("/api/bookings/my")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<String> getAuthorized(String path) {
        return restClient()
                .get()
                .uri(path)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken()))
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
