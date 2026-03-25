package com.soundstore.backend.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String genre,
        int stock,
        String imageUrl,
        boolean active,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
