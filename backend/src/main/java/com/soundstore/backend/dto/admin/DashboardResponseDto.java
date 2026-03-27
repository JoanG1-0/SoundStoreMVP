package com.soundstore.backend.dto.admin;

import java.math.BigDecimal;

public record DashboardResponseDto(
        long pedidosDelDia,
        long pedidosPendientes,
        long productosStockBajo,
        BigDecimal ventasDelMes
) {}
