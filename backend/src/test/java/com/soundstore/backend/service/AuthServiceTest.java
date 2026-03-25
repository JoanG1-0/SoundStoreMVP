package com.soundstore.backend.service;

import com.soundstore.backend.dto.auth.ForgotPasswordRequestDto;
import com.soundstore.backend.dto.auth.LoginRequestDto;
import com.soundstore.backend.dto.auth.LoginResponseDto;
import com.soundstore.backend.dto.auth.RegisterRequestDto;
import com.soundstore.backend.dto.auth.RegisterResponseDto;
import com.soundstore.backend.dto.auth.ResetPasswordRequestDto;
import com.soundstore.backend.dto.auth.VerifyEmailRequestDto;
import com.soundstore.backend.exception.EmailAlreadyExistsException;
import com.soundstore.backend.exception.OtpInvalidException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.OtpType;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private OtpService otpService;

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

    // ─── Forgot Password ─────────────────────────────────────────────────────

    @Test
    void forgotPassword_emailExistente_generaYEnviaOtp() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("juan@test.com");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        authService.forgotPassword(request);

        verify(otpService).generateAndSend("juan@test.com", OtpType.PASSWORD_RESET);
    }

    @Test
    void forgotPassword_emailNoExistente_noGeneraOtp() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("noexiste@test.com");

        when(userRepository.existsByEmail("noexiste@test.com")).thenReturn(false);

        authService.forgotPassword(request);

        verify(otpService, never()).generateAndSend(anyString(), any());
    }

    // ─── Reset Password ──────────────────────────────────────────────────────

    @Test
    void resetPassword_otpValido_actualizaContrasenia() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                "juan@test.com", "123456", "nuevaPass1");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("juan@test.com")
                .passwordHash("$2a$10$old_hash")
                .role(UserRole.BUYER)
                .active(true)
                .build();

        doNothing().when(otpService).validate("juan@test.com", "123456", OtpType.PASSWORD_RESET);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nuevaPass1")).thenReturn("$2a$10$new_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$new_hash");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("$2a$10$old_hash");
    }

    @Test
    void resetPassword_otpInvalido_lanzaExcepcion() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                "juan@test.com", "000000", "nuevaPass1");

        doThrow(new OtpInvalidException("El código OTP no es válido o ha expirado."))
                .when(otpService).validate("juan@test.com", "000000", OtpType.PASSWORD_RESET);

        assertThrows(OtpInvalidException.class, () -> authService.resetPassword(request));
        verify(userRepository, never()).save(any());
    }

    // ─── Register envía OTP ──────────────────────────────────────────────────

    @Test
    void register_exitoso_enviaOtpDeRegistro() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Juan Pérez", "juan@test.com", "pass1234", "3001234567");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(otpService).generateAndSend("juan@test.com", OtpType.REGISTRATION);
    }

    // ─── Verify Email ─────────────────────────────────────────────────────────

    @Test
    void verifyEmail_codigoValido_marcaEmailVerificado() {
        VerifyEmailRequestDto request = new VerifyEmailRequestDto("juan@test.com", "123456");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("juan@test.com")
                .role(UserRole.BUYER)
                .active(true)
                .emailVerified(false)
                .build();

        doNothing().when(otpService).validate("juan@test.com", "123456", OtpType.REGISTRATION);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.verifyEmail(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isEmailVerified()).isTrue();
    }

    @Test
    void verifyEmail_codigoInvalido_lanzaOtpInvalidException() {
        VerifyEmailRequestDto request = new VerifyEmailRequestDto("juan@test.com", "000000");

        doThrow(new OtpInvalidException("El código OTP no es válido o ha expirado."))
                .when(otpService).validate("juan@test.com", "000000", OtpType.REGISTRATION);

        assertThrows(OtpInvalidException.class, () -> authService.verifyEmail(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_usuarioNoExiste_lanzaUserNotFoundException() {
        VerifyEmailRequestDto request = new VerifyEmailRequestDto("noexiste@test.com", "123456");

        doNothing().when(otpService).validate("noexiste@test.com", "123456", OtpType.REGISTRATION);
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.verifyEmail(request));
        verify(userRepository, never()).save(any());
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
