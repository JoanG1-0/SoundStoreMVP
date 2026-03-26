package com.soundstore.backend.service;

import com.soundstore.backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private OrderEmailService orderEmailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(orderEmailService, "fromEmail", "noreply@soundstore.com");
    }

    private Order buildOrder(OrderStatus status, DeliveryType deliveryType) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Juan Comprador")
                .email("juan@test.com")
                .passwordHash("$2a$10$hash")
                .phone("3001234567")
                .role(UserRole.BUYER)
                .active(true)
                .emailVerified(true)
                .build();

        return Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("SS-20260001")
                .user(user)
                .status(status)
                .deliveryType(deliveryType)
                .total(new BigDecimal("50000.00"))
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void notificarCambioEstado_pending_enviaEmailCorrectamente() {
        Order order = buildOrder(OrderStatus.PENDING, DeliveryType.PICKUP);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getTo()).contains("juan@test.com");
        assertThat(msg.getFrom()).isEqualTo("noreply@soundstore.com");
        assertThat(msg.getSubject()).contains("SS-20260001");
        assertThat(msg.getSubject()).containsIgnoringCase("recibido");
        assertThat(msg.getText()).contains("Juan Comprador");
        assertThat(msg.getText()).contains("SS-20260001");
    }

    @Test
    void notificarCambioEstado_confirmed_asuntoContienePalabra() {
        Order order = buildOrder(OrderStatus.CONFIRMED, DeliveryType.DELIVERY);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).containsIgnoringCase("confirmado");
    }

    @Test
    void notificarCambioEstado_onTheWay_asuntoContienePalabra() {
        Order order = buildOrder(OrderStatus.ON_THE_WAY, DeliveryType.DELIVERY);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).containsIgnoringCase("camino");
    }

    @Test
    void notificarCambioEstado_readyPickup_asuntoContienePalabra() {
        Order order = buildOrder(OrderStatus.READY_PICKUP, DeliveryType.PICKUP);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).containsIgnoringCase("recoger");
    }

    @Test
    void notificarCambioEstado_delivered_asuntoContienePalabra() {
        Order order = buildOrder(OrderStatus.DELIVERED, DeliveryType.PICKUP);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).containsIgnoringCase("entregado");
    }

    @Test
    void notificarCambioEstado_cancelled_asuntoContienePalabra() {
        Order order = buildOrder(OrderStatus.CANCELLED, DeliveryType.DELIVERY);

        orderEmailService.notificarCambioEstado(order);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).containsIgnoringCase("cancelado");
        assertThat(captor.getValue().getText()).contains("SS-20260001");
    }

    @Test
    void notificarCambioEstado_siempreLlamaMailSender() {
        for (OrderStatus status : OrderStatus.values()) {
            Order order = buildOrder(status,
                    status == OrderStatus.ON_THE_WAY ? DeliveryType.DELIVERY : DeliveryType.PICKUP);
            orderEmailService.notificarCambioEstado(order);
        }
        verify(mailSender, times(OrderStatus.values().length)).send(any(SimpleMailMessage.class));
    }
}
