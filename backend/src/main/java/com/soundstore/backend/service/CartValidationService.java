package com.soundstore.backend.service;

import com.soundstore.backend.dto.cart.CartItemRequestDto;
import com.soundstore.backend.dto.cart.CartItemValidationResultDto;
import com.soundstore.backend.dto.cart.CartValidationResponseDto;
import com.soundstore.backend.model.Product;
import com.soundstore.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartValidationService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public CartValidationResponseDto validate(List<CartItemRequestDto> items) {
        List<CartItemValidationResultDto> results = items.stream()
                .map(this::validateItem)
                .toList();

        boolean valid = results.stream().allMatch(CartItemValidationResultDto::available);
        return new CartValidationResponseDto(valid, results);
    }

    private CartItemValidationResultDto validateItem(CartItemRequestDto item) {
        Optional<Product> optProduct = productRepository.findById(item.productId());

        if (optProduct.isEmpty()) {
            return new CartItemValidationResultDto(
                    item.productId(),
                    null,
                    item.quantity(),
                    0,
                    false,
                    "Producto no encontrado"
            );
        }

        Product product = optProduct.get();

        if (!product.isActive()) {
            return new CartItemValidationResultDto(
                    product.getId(),
                    product.getName(),
                    item.quantity(),
                    0,
                    false,
                    "El producto ya no esta disponible"
            );
        }

        if (product.getStock() < item.quantity()) {
            return new CartItemValidationResultDto(
                    product.getId(),
                    product.getName(),
                    item.quantity(),
                    product.getStock(),
                    false,
                    product.getStock() == 0
                            ? "Producto agotado"
                            : "Stock insuficiente. Disponibles: " + product.getStock()
            );
        }

        return new CartItemValidationResultDto(
                product.getId(),
                product.getName(),
                item.quantity(),
                product.getStock(),
                true,
                "Disponible"
        );
    }
}
