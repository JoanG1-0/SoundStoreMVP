package com.soundstore.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String fullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        String phone
) {}
