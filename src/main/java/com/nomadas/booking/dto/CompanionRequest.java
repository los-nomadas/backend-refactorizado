package com.nomadas.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CompanionRequest(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotNull
        @PastOrPresent
        LocalDate birthDate
) {
}
