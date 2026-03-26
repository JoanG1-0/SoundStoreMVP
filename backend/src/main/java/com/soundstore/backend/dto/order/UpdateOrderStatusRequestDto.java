package com.soundstore.backend.dto.order;

import com.soundstore.backend.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequestDto(
        @NotNull(message = "El nuevo estado es obligatorio")
        OrderStatus newStatus
) {}
