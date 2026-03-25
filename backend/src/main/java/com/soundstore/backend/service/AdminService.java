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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        UserRole role;
        try {
            role = UserRole.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("El rol debe ser SELLER o ADMIN");
        }

        if (role == UserRole.BUYER) {
            throw new InvalidRoleException("El rol debe ser SELLER o ADMIN");
        }

        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone().trim())
                .role(role)
                .active(true)
                .emailVerified(true)
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public UserResponseDto toggleStatus(UUID userId, ToggleStatusRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setActive(request.active());
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    private UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive(),
                user.isEmailVerified()
        );
    }
}
