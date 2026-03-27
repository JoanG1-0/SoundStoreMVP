package com.soundstore.backend.dto.order;

import com.soundstore.backend.model.DeliveryType;
import com.soundstore.backend.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        String orderNumber,
        UUID userId,
        String userName,
        String userEmail,
        OrderStatus status,
        DeliveryType deliveryType,
        String deliveryAddress,
        BigDecimal total,
        List<OrderItemResponseDto> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
