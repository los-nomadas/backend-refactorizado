package com.nomadas.trip;

import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.bus.service.BusService;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.driver.service.DriverService;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.hotel.service.HotelService;
import com.nomadas.testsupport.DomainFixtures;
import com.nomadas.trip.dto.TripCreateRequest;
import com.nomadas.trip.dto.TripResponse;
import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.model.TripStatus;
import com.nomadas.trip.repository.TripRepository;
import com.nomadas.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TripServiceTest {

    @Autowired
    private TripService tripService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private BusService busService;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        driverRepository.deleteAll();
        hotelRepository.deleteAll();
    }

    @Test
    void create_savesTrip_andResolvesAvailableStatus() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        TripResponse trip = DomainFixtures.trip(tripService, hotelId, busId, LocalDate.now().plusDays(10), 30);

        assertThat(trip.id()).isNotNull();
        assertThat(trip.status()).isEqualTo(TripStatus.AVAILABLE);
        assertThat(trip.hotelId()).isEqualTo(hotelId);
        assertThat(trip.busId()).isEqualTo(busId);
    }

    @Test
    void create_marksTripFull_whenAvailableSeatsIsZero() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        TripResponse trip = tripService.create(new TripCreateRequest(
                "Lisboa",
                "Sold out",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                hotelId,
                busId,
                BoardType.HALF_BOARD,
                new BigDecimal("100"),
                new BigDecimal("50"),
                new BigDecimal("80"),
                30,
                0,
                false,
                "https://example.com/x.png"
        ));

        assertThat(trip.status()).isEqualTo(TripStatus.FULL);
    }

    @Test
    void create_throwsConflict_whenReturnBeforeDeparture() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        TripCreateRequest request = new TripCreateRequest(
                "Lisboa", "x",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5),
                hotelId, busId,
                BoardType.FULL_BOARD,
                new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("80"),
                10, 10, false, "https://example.com/x.png"
        );

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_throwsConflict_whenTripSeatsExceedBusCapacity() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 30).id();

        TripCreateRequest request = new TripCreateRequest(
                "Lisboa", "x",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                hotelId, busId,
                BoardType.FULL_BOARD,
                new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("80"),
                50, 50, false, "https://example.com/x.png"
        );

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_throwsNotFound_whenHotelMissing() {
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();

        TripCreateRequest request = new TripCreateRequest(
                "Lisboa", "x",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                999L, busId,
                BoardType.FULL_BOARD,
                new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("80"),
                10, 10, false, "https://example.com/x.png"
        );

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOffers_returnsOnlyTripsFlaggedAsOffer() {
        Long hotelId = DomainFixtures.hotel(hotelService, "Hotel Sol", 100, 100).id();
        Long driverId = DomainFixtures.driver(driverService, "11111111A", "L-1", "d1@example.com").id();
        Long busId = DomainFixtures.bus(busService, driverId, "1111-AAA", 50).id();
        DomainFixtures.trip(tripService, hotelId, busId, LocalDate.now().plusDays(10), 30);
        tripService.create(new TripCreateRequest(
                "Paris", "no offer",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(23),
                hotelId, busId,
                BoardType.FULL_BOARD,
                new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("80"),
                30, 30, false, "https://example.com/x.png"
        ));

        assertThat(tripService.getOffers()).hasSize(1)
                .allMatch(TripResponse::isOffer);
    }
}
