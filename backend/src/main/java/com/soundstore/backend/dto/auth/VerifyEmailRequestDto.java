package com.soundstore.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequestDto(

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        String email,

        @NotBlank(message = "El código OTP es obligatorio")
        @Size(min = 6, max = 6, message = "El código OTP debe tener 6 dígitos")
        String code
) {}
