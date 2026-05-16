package com.parque.driver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DriverUpdateRequest(
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
        String licenseNumber,
        @Pattern(regexp = "^[0-9]{9,}$")
        String phone,
        @NotBlank
        @Email
        String email,
        Boolean available
) {
}
