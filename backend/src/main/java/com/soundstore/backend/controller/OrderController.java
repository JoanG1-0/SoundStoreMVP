package com.soundstore.backend.controller;

import com.soundstore.backend.dto.order.CreateOrderRequestDto;
import com.soundstore.backend.dto.order.OrderResponseDto;
import com.soundstore.backend.dto.order.UpdateOrderStatusRequestDto;
import com.soundstore.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto create(
            @Valid @RequestBody CreateOrderRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return orderService.create(request, userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public OrderResponseDto getById(@PathVariable UUID id) {
        return orderService.getById(id);
    }

    @PatchMapping("/{id}/estado")
    public OrderResponseDto updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequestDto request) {
        return orderService.updateStatus(id, request.newStatus());
    }
}
