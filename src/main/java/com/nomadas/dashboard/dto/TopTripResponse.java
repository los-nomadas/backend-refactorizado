package com.nomadas.dashboard.dto;

import java.math.BigDecimal;

public record TopTripResponse(
        Long tripId,
        String destination,
        BigDecimal revenue
) {
}
