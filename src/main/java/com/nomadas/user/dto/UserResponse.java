package com.nomadas.user.dto;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        String phone,
        LocalDate birthDate
) {
}

