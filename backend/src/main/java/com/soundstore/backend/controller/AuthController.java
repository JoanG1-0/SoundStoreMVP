package com.soundstore.backend.controller;

import com.soundstore.backend.dto.auth.ForgotPasswordRequestDto;
import com.soundstore.backend.dto.auth.LoginRequestDto;
import com.soundstore.backend.dto.auth.LoginResponseDto;
import com.soundstore.backend.dto.auth.RefreshRequestDto;
import com.soundstore.backend.dto.auth.RegisterRequestDto;
import com.soundstore.backend.dto.auth.RegisterResponseDto;
import com.soundstore.backend.dto.auth.ResetPasswordRequestDto;
import com.soundstore.backend.dto.auth.VerifyEmailRequestDto;
import com.soundstore.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponseDto refresh(@Valid @RequestBody RefreshRequestDto request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/verify-email")
    public Map<String, String> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        authService.verifyEmail(request);
        return Map.of("mensaje", "Correo verificado correctamente.");
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
        return Map.of("mensaje", "Si el correo está registrado, recibirás un código de recuperación.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return Map.of("mensaje", "Contraseña actualizada correctamente.");
    }
}
