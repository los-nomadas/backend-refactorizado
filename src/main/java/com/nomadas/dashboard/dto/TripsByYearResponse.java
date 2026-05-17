package com.nomadas.dashboard.dto;

public record TripsByYearResponse(
        int year,
        long totalTrips
) {
}
