package com.nomadas.cloudinary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Component
public class RestCloudinaryUploadClient implements CloudinaryUploadClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestCloudinaryUploadClient() {
        this.restClient = RestClient.create();
    }

    @Override
    public CloudinaryUploadResult upload(
            MultipartFile file,
            String folder,
            String cloudName,
            String apiKey,
            String apiSecret
    ) {
        try {
            ResponseEntity<String> response = restClient.post()
                    .uri("https://api.cloudinary.com/v1_1/{cloudName}/image/upload", cloudName)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(buildRequestBody(file, folder))
                    .retrieve()
                    .toEntity(String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            String imageUrl = responseBody.path("secure_url").asText();
            String publicId = responseBody.path("public_id").asText();

            if (imageUrl.isBlank() || publicId.isBlank()) {
                throw new IllegalStateException("Invalid Cloudinary response");
            }

            return new CloudinaryUploadResult(imageUrl, publicId);
        } catch (IOException exception) {
            throw new IllegalStateException("Cloudinary upload failed", exception);
        }
    }

    private MultiValueMap<String, Object> buildRequestBody(MultipartFile file, String folder) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", buildFilePart(file));
        body.add("folder", folder);
        body.add("use_filename", "true");
        body.add("unique_filename", "true");
        return body;
    }

    private HttpEntity<ByteArrayResource> buildFilePart(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
        headers.setContentDispositionFormData("file", resolveFilename(file));

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return resolveFilename(file);
            }
        };

        return new HttpEntity<>(resource, headers);
    }

    private String resolveFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image";
        }
        return originalFilename;
    }
}
