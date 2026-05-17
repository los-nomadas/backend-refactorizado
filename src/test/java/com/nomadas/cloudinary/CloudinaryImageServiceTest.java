package com.nomadas.cloudinary;

import com.nomadas.cloudinary.dto.ImageUploadResponse;
import com.nomadas.cloudinary.service.CloudinaryImageService;
import com.nomadas.cloudinary.service.CloudinaryUploadClient;
import com.nomadas.cloudinary.service.CloudinaryUploadResult;
import com.nomadas.exception.InternalServerErrorException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudinaryImageServiceTest {

    @Test
    void upload_shouldReturnImagePayload_whenUploadSucceeds() {
        CloudinaryUploadClient uploadClient = (
                file,
                folder,
                cloudName,
                apiKey,
                apiSecret
        ) -> new CloudinaryUploadResult(
                "https://res.cloudinary.com/demo/image/upload/v1/hotels/test.png",
                "hotels/test"
        );
        CloudinaryImageService imageService = new CloudinaryImageService(
                uploadClient,
                "demo",
                "key",
                "secret"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "image-content".getBytes()
        );

        ImageUploadResponse response = imageService.upload(file, "hotels");

        assertThat(response.imageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/hotels/test.png");
        assertThat(response.publicId()).isEqualTo("hotels/test");
    }

    @Test
    void upload_shouldThrowBadRequest_whenFolderIsInvalid() {
        CloudinaryImageService imageService = new CloudinaryImageService(
                (file, folder, cloudName, apiKey, apiSecret) -> null,
                "demo",
                "key",
                "secret"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "image-content".getBytes()
        );

        assertThatThrownBy(() -> imageService.upload(file, "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid image file");
    }

    @Test
    void upload_shouldThrowBadRequest_whenContentTypeIsNotImage() {
        CloudinaryImageService imageService = new CloudinaryImageService(
                (file, folder, cloudName, apiKey, apiSecret) -> null,
                "demo",
                "key",
                "secret"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not-an-image".getBytes()
        );

        assertThatThrownBy(() -> imageService.upload(file, "hotels"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid image file");
    }

    @Test
    void upload_shouldThrowInternalServerError_whenCloudinaryIsNotConfigured() {
        CloudinaryImageService imageService = new CloudinaryImageService(
                (file, folder, cloudName, apiKey, apiSecret) -> null,
                "",
                "key",
                "secret"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "image-content".getBytes()
        );

        assertThatThrownBy(() -> imageService.upload(file, "hotels"))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Image upload failed");
    }

    @Test
    void upload_shouldThrowInternalServerError_whenCloudinaryUploadFails() {
        CloudinaryImageService imageService = new CloudinaryImageService(
                (file, folder, cloudName, apiKey, apiSecret) -> {
                    throw new IllegalStateException("boom");
                },
                "demo",
                "key",
                "secret"
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "image-content".getBytes()
        );

        assertThatThrownBy(() -> imageService.upload(file, "hotels"))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Image upload failed");
    }
}
