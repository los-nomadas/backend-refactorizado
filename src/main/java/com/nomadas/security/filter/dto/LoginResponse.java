package com.nomadas.security.filter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long credentialId;
    private String username;
    private String email;
    private String role;
    private LocalDateTime expiresAt;

    public static LoginResponse of(
            String token,
            Long credentialId,
            String username,
            String email,
            String role,
            LocalDateTime expiresAt
    ) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .credentialId(credentialId)
                .username(username)
                .email(email)
                .role(role)
                .expiresAt(expiresAt)
                .build();
    }
}
