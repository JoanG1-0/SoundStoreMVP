package com.soundstore.backend.service;

import com.soundstore.backend.model.*;
import com.soundstore.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ReporteService reporteService;

    private Order buildOrder(String numero, String comprador, String email,
                              OrderStatus status, DeliveryType deliveryType, BigDecimal total) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName(comprador)
                .email(email)
                .phone("3001234567")
                .role(UserRole.BUYER)
                .build();

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderNumber(numero);
        order.setUser(user);
        order.setStatus(status);
        order.setDeliveryType(deliveryType);
        order.setTotal(total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    @Test
    void generarCsvVentas_incluyeCabecera() {
        when(orderRepository.findParaReporte(any(), any())).thenReturn(List.of());

        String csv = reporteService.generarCsvVentas(LocalDate.now(), LocalDate.now());

        assertThat(csv).startsWith("Numero Pedido,Fecha,Comprador,Email,Tipo Entrega,Estado,Total COP");
    }

    @Test
    void generarCsvVentas_conPedidos_generaFilasPorCadaUno() {
        Order o1 = buildOrder("SS-20260001", "Ana López", "ana@test.com",
                OrderStatus.DELIVERED, DeliveryType.DELIVERY, new BigDecimal("150000"));
        Order o2 = buildOrder("SS-20260002", "Carlos Ruiz", "carlos@test.com",
                OrderStatus.CONFIRMED, DeliveryType.PICKUP, new BigDecimal("80000"));

        when(orderRepository.findParaReporte(any(), any())).thenReturn(List.of(o1, o2));

        String csv = reporteService.generarCsvVentas(LocalDate.now(), LocalDate.now());
        String[] lineas = csv.split("\n");

        assertThat(lineas).hasSize(3); // cabecera + 2 filas
        assertThat(lineas[1]).contains("SS-20260001", "Ana López", "ana@test.com", "150000");
        assertThat(lineas[2]).contains("SS-20260002", "Carlos Ruiz", "carlos@test.com", "80000");
    }

    @Test
    void generarCsvVentas_sinPedidos_soloRetornaCabecera() {
        when(orderRepository.findParaReporte(any(), any())).thenReturn(List.of());

        String csv = reporteService.generarCsvVentas(LocalDate.now(), LocalDate.now());
        String[] lineas = csv.split("\n");

        assertThat(lineas).hasSize(1);
    }

    @Test
    void generarCsvVentas_nombreConComa_seEscapaCorrectamente() {
        Order o = buildOrder("SS-20260003", "García, Juan", "juan@test.com",
                OrderStatus.DELIVERED, DeliveryType.DELIVERY, new BigDecimal("50000"));

        when(orderRepository.findParaReporte(any(), any())).thenReturn(List.of(o));

        String csv = reporteService.generarCsvVentas(LocalDate.now(), LocalDate.now());

        assertThat(csv).contains("\"García, Juan\"");
    }
}
