package com.nomadas.cloudinary.dto;

public record ImageUploadResponse(
        String imageUrl,
        String publicId
) {
}

