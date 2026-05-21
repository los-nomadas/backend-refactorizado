package com.nomadas.cloudinary.service;

import com.nomadas.cloudinary.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageUploadResponse upload(MultipartFile file, String folder);
}

