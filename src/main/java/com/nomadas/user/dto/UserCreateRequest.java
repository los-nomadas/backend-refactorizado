package com.nomadas.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateRequest(
        @NotBlank
        @Size(min = 1, max = 100)
        String firstName,
        @NotBlank
        @Size(min = 1, max = 100)
        String lastName,
        @NotBlank
        @Pattern(regexp = "^[0-9]{8}[A-Z]$")
        String dni,
        @NotBlank
        @Email
        String email,
        @Pattern(regexp = "^[0-9]{9,}$")
        String phone,
        @NotNull
        @PastOrPresent
        LocalDate birthDate
) {
}

