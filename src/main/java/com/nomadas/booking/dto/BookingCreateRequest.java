package com.nomadas.booking.dto;

import com.nomadas.booking.model.GroupType;
import com.nomadas.trip.model.BoardType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingCreateRequest(
        @NotNull
        Long userId,
        @NotNull
        Long tripId,
        @NotNull
        BoardType boardType,
        @NotNull
        GroupType groupType,
        @NotEmpty
        @Valid
        List<CompanionRequest> companions
) {
}
