package com.nomadas.bus.dto;

public record BusResponse(
        Long id,
        String plateNumber,
        Integer totalSeats,
        Integer availableSeats,
        Long driverId,
        String driverFullName
) {
}
