package com.soundstore.backend.service;

import com.soundstore.backend.dto.auth.ForgotPasswordRequestDto;
import com.soundstore.backend.dto.auth.LoginRequestDto;
import com.soundstore.backend.dto.auth.LoginResponseDto;
import com.soundstore.backend.dto.auth.RegisterRequestDto;
import com.soundstore.backend.dto.auth.RegisterResponseDto;
import com.soundstore.backend.dto.auth.ResetPasswordRequestDto;
import com.soundstore.backend.dto.auth.VerifyEmailRequestDto;
import com.soundstore.backend.exception.EmailAlreadyExistsException;
import com.soundstore.backend.exception.InvalidTokenException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.OtpType;
import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.UserRepository;
import com.soundstore.backend.security.JwtService;
import com.soundstore.backend.security.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final OtpService otpService;

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone().trim())
                .role(UserRole.BUYER)
                .active(true)
                .emailVerified(false)
                .build();

        User saved = userRepository.save(user);
        otpService.generateAndSend(email, OtpType.REGISTRATION);
        return toRegisterResponseDto(saved);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequestDto request) {
        String email = request.email().toLowerCase().trim();

        otpService.validate(email, request.code(), OtpType.REGISTRATION);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        String email = request.email().toLowerCase().trim();

        // Valida credenciales — lanza BadCredentialsException o DisabledException si falla
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponseDto(accessToken, refreshToken, user.getRole().name(), user.isMustChangePassword());
    }

    public LoginResponseDto refresh(String refreshToken) {
        try {
            String email = jwtService.extractEmail(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidTokenException("El token de refresco no es válido o ha expirado");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));

            String newAccessToken = jwtService.generateAccessToken(userDetails);
            return new LoginResponseDto(newAccessToken, refreshToken, user.getRole().name(), user.isMustChangePassword());

        } catch (JwtException e) {
            throw new InvalidTokenException("El token de refresco no es válido o ha expirado");
        }
    }

    public void forgotPassword(ForgotPasswordRequestDto request) {
        String email = request.email().toLowerCase().trim();
        // Fallo silencioso si el email no existe — previene enumeración de usuarios
        if (!userRepository.existsByEmail(email)) {
            return;
        }
        otpService.generateAndSend(email, OtpType.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        String email = request.email().toLowerCase().trim();

        otpService.validate(email, request.otpCode(), OtpType.PASSWORD_RESET);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private RegisterResponseDto toRegisterResponseDto(User user) {
        return new RegisterResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isEmailVerified()
        );
    }
}
