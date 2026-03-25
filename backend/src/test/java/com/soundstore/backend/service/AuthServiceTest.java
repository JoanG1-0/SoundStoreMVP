package com.soundstore.backend.service;

import com.soundstore.backend.dto.auth.LoginRequestDto;
import com.soundstore.backend.dto.auth.LoginResponseDto;
import com.soundstore.backend.dto.auth.RegisterRequestDto;
import com.soundstore.backend.dto.auth.RegisterResponseDto;
import com.soundstore.backend.exception.EmailAlreadyExistsException;
import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.UserRepository;
import com.soundstore.backend.security.JwtService;
import com.soundstore.backend.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    // ─── Register ────────────────────────────────────────────────────────────

    @Test
    void register_exitoso_retornaResponseDto() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "juan@test.com", "pass1234", "3001234567");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .id(UUID.randomUUID())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .phone(u.getPhone())
                    .role(u.getRole())
                    .active(u.isActive())
                    .emailVerified(u.isEmailVerified())
                    .build();
        });

        RegisterResponseDto response = authService.register(request);

        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.role()).isEqualTo("BUYER");
        assertThat(response.emailVerified()).isFalse();
    }

    @Test
    void register_emailDuplicado_lanzaEmailAlreadyExistsException() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "juan@test.com", "pass1234", "3001234567");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_contraseniaNuncaSeGuardaEnTextoPlano() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "juan@test.com", "pass1234", "3001234567");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("pass1234");
    }

    @Test
    void register_emailSeNormalizaAMinusculas() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "JUAN@TEST.COM", "pass1234", "3001234567");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void register_rolAsignadoEsBuyer() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "juan@test.com", "pass1234", "3001234567");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.BUYER);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @Test
    void login_exitoso_retornaTokens() {
        LoginRequestDto request = new LoginRequestDto("juan@test.com", "pass1234");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("juan@test.com")
                .role(UserRole.BUYER)
                .active(true)
                .build();
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access.token.jwt");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh.token.jwt");

        LoginResponseDto response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access.token.jwt");
        assertThat(response.refreshToken()).isEqualTo("refresh.token.jwt");
        assertThat(response.role()).isEqualTo("BUYER");
    }

    @Test
    void login_credencialesInvalidas_lanzaBadCredentialsException() {
        LoginRequestDto request = new LoginRequestDto("juan@test.com", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_emailSeNormalizaAMinusculas() {
        LoginRequestDto request = new LoginRequestDto("JUAN@TEST.COM", "pass1234");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("juan@test.com")
                .role(UserRole.BUYER)
                .active(true)
                .build();
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        LoginResponseDto response = authService.login(request);

        verify(userRepository).findByEmail("juan@test.com");
        assertThat(response).isNotNull();
    }
}
