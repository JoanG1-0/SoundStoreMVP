package com.soundstore.backend.service;

import com.soundstore.backend.dto.user.UpdateProfileRequestDto;
import com.soundstore.backend.dto.user.UserProfileResponseDto;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("Juan Garcia")
                .email("juan@test.com")
                .passwordHash("$2a$10$hashed")
                .phone("3001234567")
                .address("Cra 15 #80-25, Bogota")
                .role(UserRole.BUYER)
                .active(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    void getProfile_usuarioExiste_retornaPerfil() {
        User user = buildUser();
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        UserProfileResponseDto result = userService.getProfile("juan@test.com");

        assertThat(result.email()).isEqualTo("juan@test.com");
        assertThat(result.fullName()).isEqualTo("Juan Garcia");
        assertThat(result.role()).isEqualTo(UserRole.BUYER);
    }

    @Test
    void getProfile_usuarioNoExiste_lanzaUserNotFoundException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getProfile("noexiste@test.com"));
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_datosValidos_actualizaCorrectamente() {
        User user = buildUser();
        UpdateProfileRequestDto request = new UpdateProfileRequestDto(
                "Juan Carlos Garcia", "3009876543", "Calle 100 #20-30, Medellin");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponseDto result = userService.updateProfile("juan@test.com", request);

        assertThat(result.fullName()).isEqualTo("Juan Carlos Garcia");
        assertThat(result.phone()).isEqualTo("3009876543");
        assertThat(result.address()).isEqualTo("Calle 100 #20-30, Medellin");
    }

    @Test
    void updateProfile_nombreSeTrima() {
        User user = buildUser();
        UpdateProfileRequestDto request = new UpdateProfileRequestDto(
                "  Juan  ", "3001234567", null);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile("juan@test.com", request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Juan");
    }

    @Test
    void updateProfile_direccionNula_setNullEnUsuario() {
        User user = buildUser();
        UpdateProfileRequestDto request = new UpdateProfileRequestDto(
                "Juan Garcia", "3001234567", null);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileResponseDto result = userService.updateProfile("juan@test.com", request);

        assertThat(result.address()).isNull();
    }

    @Test
    void updateProfile_usuarioNoExiste_lanzaUserNotFoundException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateProfile("noexiste@test.com",
                        new UpdateProfileRequestDto("X", "3001234567", null)));
        verify(userRepository, never()).save(any());
    }
}
