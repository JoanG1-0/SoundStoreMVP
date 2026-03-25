package com.soundstore.backend.controller;

import com.soundstore.backend.dto.admin.CreateUserRequestDto;
import com.soundstore.backend.dto.admin.ToggleStatusRequestDto;
import com.soundstore.backend.dto.admin.UserResponseDto;
import com.soundstore.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@Valid @RequestBody CreateUserRequestDto request) {
        return adminService.createUser(request);
    }

    @PatchMapping("/{id}/estado")
    public UserResponseDto toggleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ToggleStatusRequestDto request) {
        return adminService.toggleStatus(id, request);
    }
}
