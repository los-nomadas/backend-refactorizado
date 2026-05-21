package com.nomadas.dashboard;

import com.nomadas.booking.dto.BookingCreateRequest;
import com.nomadas.booking.dto.CompanionRequest;
import com.nomadas.booking.model.GroupType;
import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.booking.service.BookingService;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.bus.service.BusService;
import com.nomadas.dashboard.dto.CurrentYearRevenueResponse;
import com.nomadas.dashboard.dto.TopTripResponse;
import com.nomadas.dashboard.dto.TripsByYearResponse;
import com.nomadas.dashboard.service.DashboardService;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.driver.service.DriverService;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.hotel.service.HotelService;
import com.nomadas.testsupport.DomainFixtures;
import com.nomadas.trip.dto.TripCreateRequest;
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

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;
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
    private BookingService bookingService;
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

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        driverRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getTripsByYear_countsTripsDepartingInThatYear() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel A", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        int year = LocalDate.now().getYear() + 1;
        DomainFixtures.trip(tripService, hotelId, busId, LocalDate.of(year, 3, 10), 30);
        DomainFixtures.trip(tripService, hotelId, busId, LocalDate.of(year, 7, 20), 30);
        DomainFixtures.trip(tripService, hotelId, busId, LocalDate.of(year + 1, 5, 5), 30);

        TripsByYearResponse response = dashboardService.getTripsByYear(year);

        assertThat(response.year()).isEqualTo(year);
        assertThat(response.totalTrips()).isEqualTo(2);
    }

    @Test
    void getCurrentYearRevenue_sumsBookingTotals_forTripsDepartingThisYear() {
        Long userId = DomainFixtures.user(userService, "12345678A", "carla@example.com").id();
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel A", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        int currentYear = LocalDate.now().getYear();
        LocalDate departure = nextFutureDateInYear(currentYear);
        Long tripId = DomainFixtures.trip(tripService, hotelId, busId, departure, 30).id();

        bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                List.of(
                        new CompanionRequest("Carla", "Vega", LocalDate.of(1985, 3, 10)),
                        new CompanionRequest("Pablo", "Vega", LocalDate.of(1990, 1, 1))
                )
        ));

        CurrentYearRevenueResponse response = dashboardService.getCurrentYearRevenue();

        assertThat(response.year()).isEqualTo(currentYear);
        assertThat(response.totalRevenue()).isEqualByComparingTo("240.00");
    }

    @Test
    void getTopTrips_returnsTopThreeOrderedByRevenue() {
        Long userId = DomainFixtures.user(userService, "12345678A", "carla@example.com").id();
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel A", 500, 500).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 200).id();

        int year = LocalDate.now().getYear() + 1;
        Long tripHigh = tripService.create(tripRequest("Lisboa", LocalDate.of(year, 4, 1), hotelId, busId, "100", 60)).id();
        Long tripMid = tripService.create(tripRequest("Paris", LocalDate.of(year, 5, 1), hotelId, busId, "50", 60)).id();
        Long tripLow = tripService.create(tripRequest("Roma", LocalDate.of(year, 6, 1), hotelId, busId, "30", 60)).id();
        Long tripExtra = tripService.create(tripRequest("Berlin", LocalDate.of(year, 7, 1), hotelId, busId, "10", 60)).id();

        bookCompanions(userId, tripHigh, 3);
        bookCompanions(userId, tripMid, 3);
        bookCompanions(userId, tripLow, 3);
        bookCompanions(userId, tripExtra, 3);

        List<TopTripResponse> top = dashboardService.getTopTrips(year);

        assertThat(top).hasSize(3);
        assertThat(top.get(0).tripId()).isEqualTo(tripHigh);
        assertThat(top.get(0).destination()).isEqualTo("Lisboa");
        assertThat(top.get(1).tripId()).isEqualTo(tripMid);
        assertThat(top.get(2).tripId()).isEqualTo(tripLow);
    }

    private TripCreateRequest tripRequest(String destination, LocalDate departure, Long hotelId, Long busId, String adultPrice, int seats) {
        return new TripCreateRequest(
                destination,
                "fixture",
                departure,
                departure.plusDays(3),
                hotelId,
                busId,
                BoardType.FULL_BOARD,
                new BigDecimal(adultPrice),
                new BigDecimal(adultPrice),
                new BigDecimal(adultPrice),
                seats,
                seats,
                false,
                "https://example.com/x.png"
        );
    }

    private void bookCompanions(Long userId, Long tripId, int count) {
        List<CompanionRequest> companions = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new CompanionRequest("Adulto" + i, "Vega", LocalDate.of(1980, 1, 1)))
                .toList();
        bookingService.create(new BookingCreateRequest(
                userId,
                tripId,
                BoardType.FULL_BOARD,
                GroupType.NONE,
                companions
        ));
    }

    private LocalDate nextFutureDateInYear(int year) {
        LocalDate candidate = LocalDate.now().plusDays(10);
        if (candidate.getYear() != year) {
            return LocalDate.of(year, 12, 30);
        }
        return candidate;
    }
}
