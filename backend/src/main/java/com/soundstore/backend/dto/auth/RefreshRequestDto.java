package com.soundstore.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(

        @NotBlank(message = "El token de refresco es obligatorio")
        String refreshToken
) {}
