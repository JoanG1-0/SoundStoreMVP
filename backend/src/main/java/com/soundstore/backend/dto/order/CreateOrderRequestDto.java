package com.soundstore.backend.dto.order;

import com.soundstore.backend.model.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequestDto(
        @NotEmpty(message = "El pedido debe contener al menos un producto")
        @Valid
        List<OrderItemRequestDto> items,

        @NotNull(message = "La modalidad de entrega es obligatoria")
        DeliveryType deliveryType,

        String deliveryAddress
) {}
