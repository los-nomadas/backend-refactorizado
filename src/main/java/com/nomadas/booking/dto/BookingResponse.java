package com.nomadas.booking.dto;

import com.nomadas.booking.model.GroupType;
import com.nomadas.trip.model.BoardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        String userFullName,
        Long tripId,
        String tripDestination,
        BoardType boardType,
        GroupType groupType,
        BigDecimal totalPrice,
        BigDecimal groupDiscount,
        List<CompanionResponse> companions,
        LocalDateTime createdAt
) {
}
