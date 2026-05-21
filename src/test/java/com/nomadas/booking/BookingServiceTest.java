package com.nomadas.booking;

import com.nomadas.booking.dto.BookingCreateRequest;
import com.nomadas.booking.dto.BookingResponse;
import com.nomadas.booking.dto.CompanionRequest;
import com.nomadas.booking.model.GroupType;
import com.nomadas.auth.model.InternalRole;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.booking.service.BookingService;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.bus.service.BusService;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.driver.service.DriverService;
import com.nomadas.entity.InternalCredential;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.hotel.service.HotelService;
import com.nomadas.testsupport.DomainFixtures;
import com.nomadas.user.model.User;
import com.nomadas.trip.dto.TripCreateRequest;
import com.nomadas.trip.dto.TripResponse;
import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.repository.TripRepository;
import com.nomadas.trip.service.TripService;
import com.nomadas.user.repository.UserRepository;
import com.nomadas.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private BusService busService;
    @Autowired
    private TripService tripService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InternalCredentialRepository credentialRepository;

    private Long userId;
    private Long hotelId;
    private Long tripId;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        bookingRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        driverRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        userId = DomainFixtures.user(userService, "12345678A", "carla@example.com").id();
        hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();
        tripId = DomainFixtures.trip(tripService, hotelId, busId, LocalDate.now().plusDays(15), 30).id();
    }

    @Test
    void create_persistsBooking_andDecrementsCapacities() {
        BookingResponse response = bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(
                        new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)),
                        new CompanionRequest("Lucas", "Vega", LocalDate.of(2015, 7, 20))
                )
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.companions()).hasSize(2);
        assertThat(response.totalPrice()).isEqualByComparingTo("180.00");
        assertThat(response.groupDiscount()).isEqualByComparingTo("0.00");
        assertThat(tripService.getById(tripId).availableSeats()).isEqualTo(28);
        assertThat(hotelService.getById(hotelId).availablePlaces()).isEqualTo(98);
    }

    @Test
    void create_applies20PercentDiscount_whenGroupIsImserso() {
        BookingResponse response = bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.HALF_BOARD,
                GroupType.IMSERSO,
                List.of(
                        new CompanionRequest("Maria", "Vega", LocalDate.now().minusYears(70)),
                        new CompanionRequest("Jose", "Vega", LocalDate.now().minusYears(68))
                )
        ));

        assertThat(response.totalPrice()).isEqualByComparingTo("144.00");
        assertThat(response.groupDiscount()).isEqualByComparingTo("36.00");
    }

    @Test
    void create_throwsConflict_whenMinorTravelsWithoutAdult() {
        BookingCreateRequest request = new BookingCreateRequest(
                userId,
                tripId,
                BoardType.HALF_BOARD,
                GroupType.NONE,
                List.of(
                        new CompanionRequest("Lucas", "Vega", LocalDate.now().minusYears(10)),
                        new CompanionRequest("Sara", "Vega", LocalDate.now().minusYears(12))
                )
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("minor");
    }

    @Test
    void create_throwsConflict_whenTripDepartureIsInThePast() {
        TripResponse pastTrip = tripService.create(new TripCreateRequest(
                "Roma", "viaje pasado",
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(2),
                hotelId,
                busService.getAll().get(0).id(),
                BoardType.FULL_BOARD,
                new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("80"),
                10, 10, false, "https://example.com/x.png"
        ));

        BookingCreateRequest request = new BookingCreateRequest(
                userId,
                pastTrip.id(),
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)))
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("departed");
    }

    @Test
    void create_throwsConflict_whenTripDoesNotHaveEnoughSeats() {
        BookingCreateRequest tooMany = new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                seniorCompanions(31)
        );

        assertThatThrownBy(() -> bookingService.create(tooMany))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("seats");
    }

    @Test
    void create_throwsNotFound_whenUserDoesNotExist() {
        BookingCreateRequest request = new BookingCreateRequest(
                999L,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)))
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_restoresCapacities() {
        BookingResponse created = bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(
                        new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)),
                        new CompanionRequest("Pablo", "Vega", LocalDate.of(1990, 1, 1))
                )
        ));

        bookingService.delete(created.id());

        assertThat(tripService.getById(tripId).availableSeats()).isEqualTo(30);
        assertThat(hotelService.getById(hotelId).availablePlaces()).isEqualTo(100);
    }

    @Test
    void getMyBookings_returnsBookingsOfLinkedUser() {
        bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)))
        ));
        User customer = userRepository.findById(userId).orElseThrow();
        Long credentialId = linkedCredential("operator", "operator@example.com", customer);

        List<BookingResponse> myBookings = bookingService.getMyBookings(credentialId);

        assertThat(myBookings).hasSize(1);
        assertThat(myBookings.get(0).userId()).isEqualTo(userId);
    }

    @Test
    void getMyBookings_throwsNotFound_whenCredentialHasNoLinkedUser() {
        Long credentialId = linkedCredential("orphan", "orphan@example.com", null);

        assertThatThrownBy(() -> bookingService.getMyBookings(credentialId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No customer linked");
    }

    @Test
    void getMyBookings_throwsNotFound_whenCredentialDoesNotExist() {
        assertThatThrownBy(() -> bookingService.getMyBookings(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Long linkedCredential(String username, String email, User user) {
        return credentialRepository.save(InternalCredential.builder()
                .username(username)
                .email(email)
                .passwordHash("hash")
                .role(InternalRole.USER)
                .active(true)
                .user(user)
                .build()).getId();
    }

    private List<CompanionRequest> seniorCompanions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new CompanionRequest("Adulto" + i, "Vega", LocalDate.now().minusYears(40 + i % 10)))
                .toList();
    }
}
