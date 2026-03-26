package com.soundstore.backend.service;

import com.soundstore.backend.dto.order.CreateOrderRequestDto;
import com.soundstore.backend.dto.order.OrderItemRequestDto;
import com.soundstore.backend.dto.order.OrderResponseDto;
import com.soundstore.backend.exception.OrderNotFoundException;
import com.soundstore.backend.exception.ProductNotFoundException;
import com.soundstore.backend.exception.StockInsuficienteException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.*;
import com.soundstore.backend.repository.OrderRepository;
import com.soundstore.backend.repository.ProductRepository;
import com.soundstore.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User buildBuyer() {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("Comprador Test")
                .email("comprador@test.com")
                .passwordHash("$2a$10$hashed")
                .phone("3001234567")
                .address("Calle 10 # 5-30, Armenia")
                .role(UserRole.BUYER)
                .active(true)
                .emailVerified(true)
                .build();
    }

    private Product buildProduct(int stock) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("USB Salsa")
                .description("Música salsa clásica")
                .price(new BigDecimal("25000.00"))
                .genre("Salsa")
                .stock(stock)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Order buildSavedOrder(User buyer, Product product, int qty) {
        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(qty)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice().multiply(BigDecimal.valueOf(qty)))
                .build();

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("SS-20260001")
                .user(buyer)
                .status(OrderStatus.PENDING)
                .deliveryType(DeliveryType.DELIVERY)
                .deliveryAddress("Calle 10 # 5-30, Armenia")
                .total(item.getSubtotal())
                .items(new ArrayList<>(List.of(item)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        item.setOrder(order);
        return order;
    }

    // ---------------------------------------------------------------
    // create — casos felices
    // ---------------------------------------------------------------

    @Test
    void create_conDomicilio_creaOrdenCorrectamente() {
        User buyer = buildBuyer();
        Product product = buildProduct(10);
        UUID productId = product.getId();

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 2)),
                DeliveryType.DELIVERY,
                "Calle 10 # 5-30, Armenia"
        );

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.countByYear(anyInt())).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return o;
        });

        OrderResponseDto result = orderService.create(request, "comprador@test.com");

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).startsWith("SS-");
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.deliveryType()).isEqualTo(DeliveryType.DELIVERY);
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(result.items()).hasSize(1);
        assertThat(product.getStock()).isEqualTo(8);

        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void create_conPickup_noRequiereDireccion() {
        User buyer = buildBuyer();
        Product product = buildProduct(5);
        UUID productId = product.getId();

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 1)),
                DeliveryType.PICKUP,
                null
        );

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.countByYear(anyInt())).thenReturn(5L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return o;
        });

        OrderResponseDto result = orderService.create(request, "comprador@test.com");

        assertThat(result.deliveryType()).isEqualTo(DeliveryType.PICKUP);
        assertThat(result.deliveryAddress()).isNull();
        assertThat(result.orderNumber()).isEqualTo("SS-20260006");
    }

    @Test
    void create_descuentaStockCorrectamente() {
        User buyer = buildBuyer();
        Product product = buildProduct(10);
        UUID productId = product.getId();

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 3)),
                DeliveryType.PICKUP,
                null
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.countByYear(anyInt())).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            o.setCreatedAt(LocalDateTime.now());
            o.setUpdatedAt(LocalDateTime.now());
            return o;
        });

        orderService.create(request, "comprador@test.com");

        assertThat(product.getStock()).isEqualTo(7);
    }

    // ---------------------------------------------------------------
    // create — casos de error
    // ---------------------------------------------------------------

    @Test
    void create_usuarioNoExiste_lanzaUserNotFoundException() {
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(UUID.randomUUID(), 1)),
                DeliveryType.PICKUP,
                null
        );

        assertThrows(UserNotFoundException.class,
                () -> orderService.create(request, "noexiste@test.com"));
    }

    @Test
    void create_productoNoExiste_lanzaProductNotFoundException() {
        User buyer = buildBuyer();
        UUID productId = UUID.randomUUID();

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 1)),
                DeliveryType.PICKUP,
                null
        );

        assertThrows(ProductNotFoundException.class,
                () -> orderService.create(request, "comprador@test.com"));
    }

    @Test
    void create_stockInsuficiente_lanzaStockInsuficienteException() {
        User buyer = buildBuyer();
        Product product = buildProduct(2);
        UUID productId = product.getId();

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 5)),
                DeliveryType.PICKUP,
                null
        );

        assertThrows(StockInsuficienteException.class,
                () -> orderService.create(request, "comprador@test.com"));
    }

    @Test
    void create_productoInactivo_lanzaStockInsuficienteException() {
        User buyer = buildBuyer();
        Product product = buildProduct(10);
        product.setActive(false);
        UUID productId = product.getId();

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(productId, 1)),
                DeliveryType.PICKUP,
                null
        );

        assertThrows(StockInsuficienteException.class,
                () -> orderService.create(request, "comprador@test.com"));
    }

    @Test
    void create_domicilioSinDireccion_lanzaIllegalArgumentException() {
        User buyer = buildBuyer();

        when(userRepository.findByEmail("comprador@test.com")).thenReturn(Optional.of(buyer));

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                List.of(new OrderItemRequestDto(UUID.randomUUID(), 1)),
                DeliveryType.DELIVERY,
                null
        );

        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(request, "comprador@test.com"));
    }

    // ---------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------

    @Test
    void getById_existente_retornaOrdenDto() {
        User buyer = buildBuyer();
        Product product = buildProduct(5);
        Order order = buildSavedOrder(buyer, product, 2);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        OrderResponseDto result = orderService.getById(order.getId());

        assertThat(result.id()).isEqualTo(order.getId());
        assertThat(result.orderNumber()).isEqualTo("SS-20260001");
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void getById_noExistente_lanzaOrderNotFoundException() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(id));
    }
}
