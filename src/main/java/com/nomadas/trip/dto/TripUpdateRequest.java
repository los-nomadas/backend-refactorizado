package com.nomadas.trip.dto;

import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.model.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TripUpdateRequest(
        @NotBlank
        String destination,
        @NotBlank
        String description,
        @NotNull
        LocalDate departureDate,
        @NotNull
        LocalDate returnDate,
        @NotNull
        Long hotelId,
        @NotNull
        Long busId,
        @NotNull
        BoardType boardType,
        @NotNull
        @Positive
        BigDecimal priceAdult,
        @NotNull
        @Positive
        BigDecimal priceChild,
        @NotNull
        @Positive
        BigDecimal priceSenior,
        @NotNull
        @Positive
        Integer totalSeats,
        @NotNull
        @PositiveOrZero
        Integer availableSeats,
        @NotNull
        Boolean isOffer,
        @NotNull
        TripStatus status,
        @NotBlank
        String imageUrl
) {
}
