package com.soundstore.backend.controller;

import com.soundstore.backend.dto.user.ChangePasswordRequestDto;
import com.soundstore.backend.dto.user.UpdateProfileRequestDto;
import com.soundstore.backend.dto.user.UserProfileResponseDto;
import com.soundstore.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponseDto getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getProfile(userDetails.getUsername());
    }

    @PutMapping("/me")
    public UserProfileResponseDto updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        return userService.updateProfile(userDetails.getUsername(), request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(userDetails.getUsername(), request);
    }
}
