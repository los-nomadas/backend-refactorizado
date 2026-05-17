package com.parque.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parque.auth.model.InternalRole;
import com.parque.auth.repository.InternalCredentialRepository;
import com.parque.entity.InternalCredential;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

public final class InternalAuthSupport {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_EMAIL = "admin@nomadas.local";
    public static final String ADMIN_PASSWORD = "admin12345";

    private InternalAuthSupport() {
    }

    public static void ensureAdminCredential(
            InternalCredentialRepository internalCredentialRepository,
            PasswordEncoder passwordEncoder
    ) {
        if (internalCredentialRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            return;
        }

        internalCredentialRepository.save(InternalCredential.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(InternalRole.ADMIN)
                .active(true)
                .build());
    }

    public static String login(RestClient restClient, ObjectMapper objectMapper) throws Exception {
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(ADMIN_USERNAME, ADMIN_PASSWORD);

        ResponseEntity<String> response = restClient
                .post()
                .uri("/api/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = objectMapper.readTree(response.getBody());
        return json.get("token").asText();
    }
}
