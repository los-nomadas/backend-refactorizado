package com.nomadas.dashboard.dto;

import java.math.BigDecimal;

public record CurrentYearRevenueResponse(
        int year,
        BigDecimal totalRevenue
) {
}
