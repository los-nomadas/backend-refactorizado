package com.nomadas.booking.dto;

import java.time.LocalDate;

public record CompanionResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate
) {
}
