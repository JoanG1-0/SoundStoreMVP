package com.soundstore.backend.dto.admin;

import jakarta.validation.constraints.NotNull;

public record ToggleStatusRequestDto(

        @NotNull(message = "El campo 'active' es obligatorio")
        Boolean active
) {}
