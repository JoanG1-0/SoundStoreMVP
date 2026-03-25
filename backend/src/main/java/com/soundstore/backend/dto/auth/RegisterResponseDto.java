package com.soundstore.backend.dto.auth;

import java.util.UUID;

public record RegisterResponseDto(
        UUID id,
        String fullName,
        String email,
        String phone,
        String role,
        boolean emailVerified
) {}
