package com.nomadas.cloudinary.service;

import com.nomadas.cloudinary.dto.ImageUploadResponse;
import com.nomadas.exception.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class CloudinaryImageService implements ImageService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of("hotels", "attractions", "offers", "employees");

    private final CloudinaryUploadClient cloudinaryUploadClient;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryImageService(
            CloudinaryUploadClient cloudinaryUploadClient,
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret
    ) {
        this.cloudinaryUploadClient = cloudinaryUploadClient;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public ImageUploadResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Invalid image file");
        }
        if (folder == null || !ALLOWED_FOLDERS.contains(folder)) {
            throw new IllegalArgumentException("Invalid image file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image file");
        }
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new InternalServerErrorException("Image upload failed");
        }

        try {
            CloudinaryUploadResult uploaded = cloudinaryUploadClient.upload(file, folder, cloudName, apiKey, apiSecret);
            return new ImageUploadResponse(uploaded.imageUrl(), uploaded.publicId());
        } catch (Exception exception) {
            throw new InternalServerErrorException("Image upload failed");
        }
    }
}
