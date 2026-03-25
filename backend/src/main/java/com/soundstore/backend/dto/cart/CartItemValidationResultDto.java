package com.soundstore.backend.dto.cart;

import java.util.UUID;

public record CartItemValidationResultDto(
        UUID productId,
        String productName,
        int requestedQuantity,
        int availableStock,
        boolean available,
        String message
) {}
