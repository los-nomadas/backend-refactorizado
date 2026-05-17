package com.nomadas.cloudinary.service;

public record CloudinaryUploadResult(
        String imageUrl,
        String publicId
) {
}
