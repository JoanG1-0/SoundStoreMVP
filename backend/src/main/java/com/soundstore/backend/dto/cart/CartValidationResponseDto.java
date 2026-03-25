package com.soundstore.backend.dto.cart;

import java.util.List;

public record CartValidationResponseDto(
        boolean valid,
        List<CartItemValidationResultDto> items
) {}
