package com.soundstore.backend.service;

import com.soundstore.backend.dto.auth.LoginRequestDto;
import com.soundstore.backend.dto.auth.LoginResponseDto;
import com.soundstore.backend.dto.auth.RegisterRequestDto;
import com.soundstore.backend.dto.auth.RegisterResponseDto;
import com.soundstore.backend.exception.EmailAlreadyExistsException;
import com.soundstore.backend.exception.InvalidTokenException;
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
        return toRegisterResponseDto(saved);
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

        return new LoginResponseDto(accessToken, refreshToken, user.getRole().name());
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
            return new LoginResponseDto(newAccessToken, refreshToken, user.getRole().name());

        } catch (JwtException e) {
            throw new InvalidTokenException("El token de refresco no es válido o ha expirado");
        }
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
