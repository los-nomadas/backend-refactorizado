package com.nomadas.testsupport;

import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.dto.BusResponse;
import com.nomadas.bus.service.BusService;
import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.dto.DriverResponse;
import com.nomadas.driver.service.DriverService;
import com.nomadas.hotel.dto.HotelCreateRequest;
import com.nomadas.hotel.dto.HotelResponse;
import com.nomadas.hotel.service.HotelService;
import com.nomadas.trip.dto.TripCreateRequest;
import com.nomadas.trip.dto.TripResponse;
import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.service.TripService;
import com.nomadas.user.dto.UserCreateRequest;
import com.nomadas.user.dto.UserResponse;
import com.nomadas.user.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DomainFixtures {

    private DomainFixtures() {
    }

    public static UserResponse user(UserService userService, String dni, String email) {
        return userService.create(new UserCreateRequest(
                "Carla",
                "Vega",
                dni,
                email,
                "611222333",
                LocalDate.of(1985, 3, 10)
        ));
    }

    public static HotelResponse hotel(HotelService hotelService, String name, int totalPlaces, int availablePlaces) {
        return hotelService.create(new HotelCreateRequest(
                name,
                "Hotel para fixture de tests",
                "Madrid",
                40,
                40,
                totalPlaces,
                availablePlaces,
                new BigDecimal("60.00"),
                new BigDecimal("90.00"),
                "https://example.com/hotel.png"
        ));
    }

    public static DriverResponse driver(DriverService driverService, String dni, String license, String email) {
        return driverService.create(new DriverCreateRequest(
                "Marta",
                "Ortega",
                dni,
                license,
                "611222333",
                email,
                true
        ));
    }

    public static BusResponse bus(BusService busService, Long driverId, String plate, int seats) {
        return busService.create(new BusCreateRequest(plate, seats, seats, driverId));
    }

    public static TripResponse trip(
            TripService tripService,
            Long hotelId,
            Long busId,
            LocalDate departureDate,
            int seats
    ) {
        return tripService.create(new TripCreateRequest(
                "Lisboa",
                "Escapada de fin de semana",
                departureDate,
                departureDate.plusDays(3),
                hotelId,
                busId,
                BoardType.FULL_BOARD,
                new BigDecimal("120.00"),
                new BigDecimal("60.00"),
                new BigDecimal("90.00"),
                seats,
                seats,
                true,
                "https://example.com/trip.png"
        ));
    }
}
