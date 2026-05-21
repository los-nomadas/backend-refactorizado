package com.nomadas.trip.repository;

import com.nomadas.trip.model.Trip;
import com.nomadas.trip.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByIsOfferTrue();

    List<Trip> findByStatus(TripStatus status);

    List<Trip> findByDepartureDateBetween(LocalDate start, LocalDate end);

    boolean existsByBusIdAndStatusIn(Long busId, List<TripStatus> statuses);

    boolean existsByHotelIdAndStatusIn(Long hotelId, List<TripStatus> statuses);
}
