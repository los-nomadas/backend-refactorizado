package com.parque.bus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record BusUpdateRequest(
        @NotBlank
        String plateNumber,
        @NotNull
        @Positive
        Integer totalSeats,
        @NotNull
        @PositiveOrZero
        Integer availableSeats,
        @NotNull
        Long driverId
) {
}
