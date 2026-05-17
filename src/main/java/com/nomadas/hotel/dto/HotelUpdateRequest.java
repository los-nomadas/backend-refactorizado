package com.nomadas.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record HotelUpdateRequest(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotBlank
        String location,
        @NotNull
        @Positive
        Integer totalRooms,
        @NotNull
        @PositiveOrZero
        Integer availableRooms,
        @NotNull
        @Positive
        Integer totalPlaces,
        @NotNull
        @PositiveOrZero
        Integer availablePlaces,
        @NotNull
        @Positive
        BigDecimal halfBoardPrice,
        @NotNull
        @Positive
        BigDecimal fullBoardPrice,
        @NotBlank
        String imageUrl
) {
}

