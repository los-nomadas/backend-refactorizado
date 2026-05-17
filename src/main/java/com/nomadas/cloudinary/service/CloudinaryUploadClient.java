package com.nomadas.cloudinary.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryUploadClient {
    CloudinaryUploadResult upload(MultipartFile file, String folder, String cloudName, String apiKey, String apiSecret);
}
