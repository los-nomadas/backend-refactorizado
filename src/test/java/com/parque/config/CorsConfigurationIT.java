package com.parque.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CorsConfigurationIT {

    @LocalServerPort
    private int port;

    @Test
    void optionsRequest_shouldAllowLocalFrontendRunningOnAnyPort() {
        ResponseEntity<Void> response = restClient()
                .method(HttpMethod.OPTIONS)
                .uri("/api/hotels")
                .header(HttpHeaders.ORIGIN, "http://127.0.0.1:5174")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://127.0.0.1:5174");
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
