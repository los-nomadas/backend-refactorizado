package com.nomadas.trip.dto;

import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.model.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TripResponse(
        Long id,
        String destination,
        String description,
        LocalDate departureDate,
        LocalDate returnDate,
        Long hotelId,
        String hotelName,
        Long busId,
        String busPlateNumber,
        BoardType boardType,
        BigDecimal priceAdult,
        BigDecimal priceChild,
        BigDecimal priceSenior,
        Integer totalSeats,
        Integer availableSeats,
        Boolean isOffer,
        TripStatus status,
        String imageUrl
) {
}
