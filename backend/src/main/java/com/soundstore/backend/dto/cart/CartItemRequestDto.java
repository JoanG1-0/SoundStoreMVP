package com.soundstore.backend.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemRequestDto(
        @NotNull UUID productId,
        @Min(1) int quantity
) {}
