package com.soundstore.backend.service;

import com.soundstore.backend.dto.admin.DashboardResponseDto;
import com.soundstore.backend.model.OrderStatus;
import com.soundstore.backend.repository.OrderRepository;
import com.soundstore.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getMetricas_retornaTodosLosValoresCorrectamente() {
        LocalDate hoy = LocalDate.now();

        when(orderRepository.countToday()).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(3L);
        when(productRepository.countByActiveTrueAndStockLessThan(5)).thenReturn(2L);
        when(orderRepository.sumVentasMes(eq(hoy.getYear()), eq(hoy.getMonthValue())))
                .thenReturn(new BigDecimal("1500000.00"));

        DashboardResponseDto result = dashboardService.getMetricas();

        assertThat(result.pedidosDelDia()).isEqualTo(5L);
        assertThat(result.pedidosPendientes()).isEqualTo(3L);
        assertThat(result.productosStockBajo()).isEqualTo(2L);
        assertThat(result.ventasDelMes()).isEqualByComparingTo(new BigDecimal("1500000.00"));
    }

    @Test
    void getMetricas_sinPedidosHoy_retornaCero() {
        LocalDate hoy = LocalDate.now();

        when(orderRepository.countToday()).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(0L);
        when(productRepository.countByActiveTrueAndStockLessThan(5)).thenReturn(0L);
        when(orderRepository.sumVentasMes(eq(hoy.getYear()), eq(hoy.getMonthValue())))
                .thenReturn(BigDecimal.ZERO);

        DashboardResponseDto result = dashboardService.getMetricas();

        assertThat(result.pedidosDelDia()).isZero();
        assertThat(result.pedidosPendientes()).isZero();
        assertThat(result.productosStockBajo()).isZero();
        assertThat(result.ventasDelMes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getMetricas_ventasDelMes_noCuentaPedidosCancelados() {
        LocalDate hoy = LocalDate.now();

        // sumVentasMes excluye CANCELLED a nivel de query — verificamos que el valor
        // retornado por el repo se propaga sin modificación al DTO
        when(orderRepository.countToday()).thenReturn(10L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(2L);
        when(productRepository.countByActiveTrueAndStockLessThan(5)).thenReturn(1L);
        when(orderRepository.sumVentasMes(eq(hoy.getYear()), eq(hoy.getMonthValue())))
                .thenReturn(new BigDecimal("800000.00"));

        DashboardResponseDto result = dashboardService.getMetricas();

        assertThat(result.ventasDelMes()).isEqualByComparingTo(new BigDecimal("800000.00"));
    }

    @Test
    void getMetricas_stockBajo_soloProductosActivos() {
        LocalDate hoy = LocalDate.now();

        when(orderRepository.countToday()).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(0L);
        when(productRepository.countByActiveTrueAndStockLessThan(5)).thenReturn(4L);
        when(orderRepository.sumVentasMes(eq(hoy.getYear()), eq(hoy.getMonthValue())))
                .thenReturn(BigDecimal.ZERO);

        DashboardResponseDto result = dashboardService.getMetricas();

        assertThat(result.productosStockBajo()).isEqualTo(4L);
    }
}
