package com.soundstore.backend.service;

import com.soundstore.backend.dto.admin.CreateUserRequestDto;
import com.soundstore.backend.dto.admin.ToggleStatusRequestDto;
import com.soundstore.backend.dto.admin.UserResponseDto;
import com.soundstore.backend.exception.EmailAlreadyExistsException;
import com.soundstore.backend.exception.InvalidRoleException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    // ─── createUser ──────────────────────────────────────────────────────────

    @Test
    void createUser_rolVendedor_creaUsuarioCorrectamente() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Ana Vendedora", "ana@test.com", "pass1234", "3001234567", "SELLER");

        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
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

        UserResponseDto response = adminService.createUser(request);

        assertThat(response.email()).isEqualTo("ana@test.com");
        assertThat(response.role()).isEqualTo("SELLER");
        assertThat(response.active()).isTrue();
        assertThat(response.emailVerified()).isTrue();
    }

    @Test
    void createUser_rolAdmin_creaUsuarioCorrectamente() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Carlos Admin", "carlos@test.com", "pass1234", "3007654321", "ADMIN");

        when(userRepository.existsByEmail("carlos@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
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

        UserResponseDto response = adminService.createUser(request);

        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void createUser_rolBuyer_lanzaInvalidRoleException() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Juan", "juan@test.com", "pass1234", "3001234567", "BUYER");

        assertThrows(InvalidRoleException.class, () -> adminService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_rolInvalido_lanzaInvalidRoleException() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Juan", "juan@test.com", "pass1234", "3001234567", "SUPERADMIN");

        assertThrows(InvalidRoleException.class, () -> adminService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_emailDuplicado_lanzaEmailAlreadyExistsException() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Ana", "ana@test.com", "pass1234", "3001234567", "SELLER");

        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> adminService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_emailSeNormalizaAMinusculas() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Ana", "ANA@TEST.COM", "pass1234", "3001234567", "SELLER");

        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    void createUser_emailVerifiedEsTrue() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Ana", "ana@test.com", "pass1234", "3001234567", "SELLER");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isEmailVerified()).isTrue();
    }

    // ─── toggleStatus ─────────────────────────────────────────────────────────

    @Test
    void toggleStatus_desactivaUsuario() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Ana")
                .email("ana@test.com")
                .phone("3001234567")
                .role(UserRole.SELLER)
                .active(true)
                .emailVerified(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto response = adminService.toggleStatus(userId, new ToggleStatusRequestDto(false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void toggleStatus_activaUsuario() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("Ana")
                .email("ana@test.com")
                .phone("3001234567")
                .role(UserRole.SELLER)
                .active(false)
                .emailVerified(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto response = adminService.toggleStatus(userId, new ToggleStatusRequestDto(true));

        assertThat(response.active()).isTrue();
    }

    @Test
    void toggleStatus_usuarioNoExiste_lanzaUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminService.toggleStatus(userId, new ToggleStatusRequestDto(false)));
        verify(userRepository, never()).save(any());
    }
}
