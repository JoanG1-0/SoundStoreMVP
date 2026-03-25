package com.soundstore.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.soundstore.backend.exception.ImageUploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    void uploadImage_archivoValido_retornaSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "producto.jpg", "image/jpeg", "contenido".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/imagen.jpg"));

        String url = cloudinaryService.uploadImage(file);

        assertThat(url).isEqualTo("https://res.cloudinary.com/test/imagen.jpg");
    }

    @Test
    void uploadImage_errorDeCloudinary_lanzaImageUploadException() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "producto.jpg", "image/jpeg", "contenido".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new IOException("conexión fallida"));

        assertThrows(ImageUploadException.class, () -> cloudinaryService.uploadImage(file));
    }

    @Test
    void uploadImage_llamaConCarpetaCorrecta() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "producto.jpg", "image/jpeg", "contenido".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test/imagen.jpg"));

        cloudinaryService.uploadImage(file);

        verify(uploader).upload(any(byte[].class), argThat(opts ->
                opts instanceof Map && "soundstore/products".equals(((Map<?, ?>) opts).get("folder"))
        ));
    }
}
