package com.nomadas.cloudinary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.cloudinary.service.CloudinaryUploadClient;
import com.nomadas.cloudinary.service.CloudinaryUploadResult;
import com.nomadas.testconfig.JacksonTestConfig;
import com.nomadas.testsupport.InternalAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "CLOUDINARY_CLOUD_NAME=test-cloud",
                "CLOUDINARY_API_KEY=test-key",
                "CLOUDINARY_API_SECRET=test-secret"
        }
)
@Import(JacksonTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ImageControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalCredentialRepository internalCredentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CloudinaryUploadClient cloudinaryUploadClient;

    @Test
    void upload_shouldReturn400_whenInvalidFile() throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("folder", "hotels");
        body.add("file", multipartFile("test.txt", "text/plain", "x".getBytes()));

        ResponseEntity<String> response = restClient()
                .post()
                .uri("/api/images/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertErrorContract(json, 400, "Bad Request", "Invalid image file", "/api/images/upload");
    }

    @Test
    void upload_shouldReturn400_whenFolderIsInvalid() throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("folder", "invalid");
        body.add("file", multipartFile("test.png", "image/png", "x".getBytes()));

        ResponseEntity<String> response = restClient()
                .post()
                .uri("/api/images/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertErrorContract(json, 400, "Bad Request", "Invalid image file", "/api/images/upload");
    }

    @Test
    void upload_shouldReturn201AndImagePayload_whenUploadSucceeds() throws Exception {
        when(cloudinaryUploadClient.upload(any(), eq("hotels"), eq("test-cloud"), eq("test-key"), eq("test-secret")))
                .thenReturn(new CloudinaryUploadResult(
                        "https://res.cloudinary.com/demo/image/upload/v1/hotels/test.png",
                        "hotels/test"
                ));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("folder", "hotels");
        body.add("file", multipartFile("test.png", "image/png", "image-content".getBytes()));

        ResponseEntity<String> response = restClient()
                .post()
                .uri("/api/images/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType().toString()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(fieldNames(json)).containsExactly("imageUrl", "publicId");
        assertThat(json.get("imageUrl").asText()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/hotels/test.png");
        assertThat(json.get("publicId").asText()).isEqualTo("hotels/test");
    }

    @Test
    void upload_shouldReturn500_whenCloudinaryUploadFails() throws Exception {
        when(cloudinaryUploadClient.upload(any(), eq("hotels"), eq("test-cloud"), eq("test-key"), eq("test-secret")))
                .thenThrow(new IllegalStateException("boom"));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("folder", "hotels");
        body.add("file", multipartFile("test.png", "image/png", "image-content".getBytes()));

        ResponseEntity<String> response = restClient()
                .post()
                .uri("/api/images/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        JsonNode json = objectMapper.readTree(response.getBody());
        assertErrorContract(json, 500, "Internal Server Error", "Image upload failed", "/api/images/upload");
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }

    private String authToken() {
        try {
            InternalAuthSupport.ensureAdminCredential(internalCredentialRepository, passwordEncoder);
            return InternalAuthSupport.login(restClient(), objectMapper);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private HttpEntity<ByteArrayResource> multipartFile(String filename, String contentType, byte[] content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("file", filename);

        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        return new HttpEntity<>(resource, headers);
    }

    private void assertErrorContract(JsonNode body, int status, String error, String message, String path) {
        assertThat(fieldNames(body)).containsExactly("status", "error", "message", "path", "timestamp");
        assertThat(body.get("status").asInt()).isEqualTo(status);
        assertThat(body.get("error").asText()).isEqualTo(error);
        assertThat(body.get("message").asText()).isEqualTo(message);
        assertThat(body.get("path").asText()).isEqualTo(path);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    private List<String> fieldNames(JsonNode node) {
        List<String> fieldNames = new ArrayList<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        return fieldNames;
    }
}
