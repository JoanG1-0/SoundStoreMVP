package com.soundstore.backend.dto.auth;

public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String role
) {}
