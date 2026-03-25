package com.soundstore.backend.dto.user;

import com.soundstore.backend.model.UserRole;

import java.util.UUID;

public record UserProfileResponseDto(
        UUID id,
        String fullName,
        String email,
        String phone,
        String address,
        UserRole role,
        boolean active,
        boolean emailVerified
) {}
