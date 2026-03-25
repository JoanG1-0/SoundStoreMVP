package com.soundstore.backend.service;

import com.soundstore.backend.dto.cart.CartItemRequestDto;
import com.soundstore.backend.dto.cart.CartValidationResponseDto;
import com.soundstore.backend.model.Product;
import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartValidationServiceTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartValidationService cartValidationService;

    private Product buildProduct(int stock, boolean active) {
        User seller = User.builder()
                .id(UUID.randomUUID())
                .fullName("Vendedor")
                .email("v@test.com")
                .passwordHash("hash")
                .phone("3001234567")
                .role(UserRole.SELLER)
                .active(true)
                .emailVerified(true)
                .build();

        return Product.builder()
                .id(UUID.randomUUID())
                .name("USB Salsa")
                .description("Descripcion")
                .price(new BigDecimal("25000.00"))
                .genre("Salsa")
                .stock(stock)
                .active(active)
                .createdBy(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void validate_stockSuficiente_retornaValido() {
        Product product = buildProduct(10, true);
        CartItemRequestDto item = new CartItemRequestDto(product.getId(), 3);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartValidationResponseDto result = cartValidationService.validate(List.of(item));

        assertThat(result.valid()).isTrue();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).available()).isTrue();
        assertThat(result.items().get(0).availableStock()).isEqualTo(10);
    }

    @Test
    void validate_stockInsuficiente_retornaNoValido() {
        Product product = buildProduct(2, true);
        CartItemRequestDto item = new CartItemRequestDto(product.getId(), 5);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartValidationResponseDto result = cartValidationService.validate(List.of(item));

        assertThat(result.valid()).isFalse();
        assertThat(result.items().get(0).available()).isFalse();
        assertThat(result.items().get(0).availableStock()).isEqualTo(2);
    }

    @Test
    void validate_productoAgotado_retornaNoValido() {
        Product product = buildProduct(0, true);
        CartItemRequestDto item = new CartItemRequestDto(product.getId(), 1);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartValidationResponseDto result = cartValidationService.validate(List.of(item));

        assertThat(result.valid()).isFalse();
        assertThat(result.items().get(0).message()).isEqualTo("Producto agotado");
    }

    @Test
    void validate_productoInactivo_retornaNoValido() {
        Product product = buildProduct(10, false);
        CartItemRequestDto item = new CartItemRequestDto(product.getId(), 1);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        CartValidationResponseDto result = cartValidationService.validate(List.of(item));

        assertThat(result.valid()).isFalse();
        assertThat(result.items().get(0).message()).isEqualTo("El producto ya no esta disponible");
    }

    @Test
    void validate_productoNoExiste_retornaNoValido() {
        UUID id = UUID.randomUUID();
        CartItemRequestDto item = new CartItemRequestDto(id, 1);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        CartValidationResponseDto result = cartValidationService.validate(List.of(item));

        assertThat(result.valid()).isFalse();
        assertThat(result.items().get(0).message()).isEqualTo("Producto no encontrado");
    }

    @Test
    void validate_variosItems_unConProblema_retornaNoValido() {
        Product p1 = buildProduct(10, true);
        Product p2 = buildProduct(1, true);

        CartItemRequestDto item1 = new CartItemRequestDto(p1.getId(), 2);
        CartItemRequestDto item2 = new CartItemRequestDto(p2.getId(), 5);

        when(productRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
        when(productRepository.findById(p2.getId())).thenReturn(Optional.of(p2));

        CartValidationResponseDto result = cartValidationService.validate(List.of(item1, item2));

        assertThat(result.valid()).isFalse();
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).available()).isTrue();
        assertThat(result.items().get(1).available()).isFalse();
    }

    @Test
    void validate_listaVacia_retornaValido() {
        CartValidationResponseDto result = cartValidationService.validate(List.of());

        assertThat(result.valid()).isTrue();
        assertThat(result.items()).isEmpty();
    }
}
