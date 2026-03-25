package com.soundstore.backend.controller;

import com.soundstore.backend.dto.cart.CartItemRequestDto;
import com.soundstore.backend.dto.cart.CartValidationResponseDto;
import com.soundstore.backend.service.CartValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartValidationService cartValidationService;

    @PostMapping("/validate")
    public CartValidationResponseDto validate(
            @Valid @RequestBody List<@Valid CartItemRequestDto> items) {
        return cartValidationService.validate(items);
    }
}
