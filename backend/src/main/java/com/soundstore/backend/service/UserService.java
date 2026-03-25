package com.soundstore.backend.service;

import com.soundstore.backend.dto.user.UpdateProfileRequestDto;
import com.soundstore.backend.dto.user.UserProfileResponseDto;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.User;
import com.soundstore.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return toDto(user);
    }

    @Transactional
    public UserProfileResponseDto updateProfile(String email, UpdateProfileRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone().trim());
        user.setAddress(request.address() != null ? request.address().trim() : null);

        return toDto(userRepository.save(user));
    }

    private UserProfileResponseDto toDto(User user) {
        return new UserProfileResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified()
        );
    }
}
