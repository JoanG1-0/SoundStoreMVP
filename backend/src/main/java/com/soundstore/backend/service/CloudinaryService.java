package com.soundstore.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.soundstore.backend.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "soundstore/products",
                            "resource_type", "image"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new ImageUploadException("Error al subir la imagen. Intente de nuevo.");
        }
    }
}
