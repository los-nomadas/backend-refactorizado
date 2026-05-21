package com.nomadas.driver.dto;

public record DriverResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String licenseNumber,
        String phone,
        String email,
        Boolean available
) {
}
