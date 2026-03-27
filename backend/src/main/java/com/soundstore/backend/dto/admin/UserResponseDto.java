package com.soundstore.backend.dto.admin;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String fullName,
        String email,
        String phone,
        String role,
        boolean active,
        boolean emailVerified,
        boolean mustChangePassword
) {}
