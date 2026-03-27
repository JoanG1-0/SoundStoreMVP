package com.soundstore.backend.service;

import com.soundstore.backend.dto.admin.DashboardResponseDto;
import com.soundstore.backend.model.OrderStatus;
import com.soundstore.backend.repository.OrderRepository;
import com.soundstore.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDto getMetricas() {
        LocalDate hoy = LocalDate.now();

        long pedidosDelDia       = orderRepository.countToday();
        long pedidosPendientes   = orderRepository.countByStatus(OrderStatus.PENDING);
        long productosStockBajo  = productRepository.countByActiveTrueAndStockLessThan(5);
        BigDecimal ventasDelMes  = orderRepository.sumVentasMes(hoy.getYear(), hoy.getMonthValue());

        return new DashboardResponseDto(
                pedidosDelDia,
                pedidosPendientes,
                productosStockBajo,
                ventasDelMes
        );
    }
}
