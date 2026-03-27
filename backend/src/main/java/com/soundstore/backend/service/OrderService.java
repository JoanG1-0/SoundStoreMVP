package com.soundstore.backend.service;

import com.soundstore.backend.dto.order.CreateOrderRequestDto;
import com.soundstore.backend.dto.order.OrderItemRequestDto;
import com.soundstore.backend.dto.order.OrderItemResponseDto;
import com.soundstore.backend.dto.order.OrderResponseDto;
import com.soundstore.backend.exception.OrderNotFoundException;
import com.soundstore.backend.exception.ProductNotFoundException;
import com.soundstore.backend.exception.StockInsuficienteException;
import com.soundstore.backend.exception.TransicionEstadoInvalidaException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.*;
import com.soundstore.backend.repository.OrderRepository;
import com.soundstore.backend.repository.ProductRepository;
import com.soundstore.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderEmailService orderEmailService;

    @Transactional
    public OrderResponseDto create(CreateOrderRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (request.deliveryType() == DeliveryType.DELIVERY
                && (request.deliveryAddress() == null || request.deliveryAddress().isBlank())) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria para pedidos a domicilio");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDto itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Producto no encontrado: " + itemReq.productId()));

            if (!product.isActive()) {
                throw new StockInsuficienteException(
                        "El producto '" + product.getName() + "' no está disponible");
            }

            if (product.getStock() < itemReq.quantity()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para '" + product.getName()
                        + "'. Disponible: " + product.getStock()
                        + ", solicitado: " + itemReq.quantity());
            }

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());
        }

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(OrderStatus.PENDING)
                .deliveryType(request.deliveryType())
                .deliveryAddress(request.deliveryType() == DeliveryType.DELIVERY
                        ? request.deliveryAddress().trim()
                        : null)
                .total(total)
                .items(new ArrayList<>())
                .build();

        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);
        orderEmailService.notificarCambioEstado(saved);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> misPedidos(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> listActivos() {
        return orderRepository.findActivos(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED))
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado"));
        return toDto(order);
    }

    @Transactional
    public OrderResponseDto updateStatus(UUID id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado"));

        validarTransicion(order, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            restituirStock(order);
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        orderEmailService.notificarCambioEstado(saved);
        return toDto(saved);
    }

    private void validarTransicion(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        DeliveryType delivery = order.getDeliveryType();

        boolean valida = switch (current) {
            case PENDING    -> newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED  -> newStatus == OrderStatus.PREPARING
                            || newStatus == OrderStatus.CANCELLED;
            case PREPARING  -> (newStatus == OrderStatus.ON_THE_WAY   && delivery == DeliveryType.DELIVERY)
                            || (newStatus == OrderStatus.READY_PICKUP  && delivery == DeliveryType.PICKUP);
            case ON_THE_WAY    -> newStatus == OrderStatus.DELIVERED;
            case READY_PICKUP  -> newStatus == OrderStatus.DELIVERED;
            default         -> false;
        };

        if (!valida) {
            throw new TransicionEstadoInvalidaException(
                    "Transición no permitida: " + current + " → " + newStatus);
        }
    }

    private void restituirStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado al restituir stock"));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private String generateOrderNumber() {
        int year = LocalDateTime.now().getYear();
        long count = orderRepository.countByYear(year);
        return String.format("SS-%d%04d", year, count + 1);
    }

    private OrderResponseDto toDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getStatus(),
                order.getDeliveryType(),
                order.getDeliveryAddress(),
                order.getTotal(),
                itemDtos,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
