package com.soundstore.backend.dto.product;

import jakarta.validation.constraints.NotNull;

public record ToggleProductStatusRequestDto(
        @NotNull(message = "El campo 'activo' es obligatorio")
        Boolean active
) {}
