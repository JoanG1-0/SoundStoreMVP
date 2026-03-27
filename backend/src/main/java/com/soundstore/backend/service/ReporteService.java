package com.soundstore.backend.service;

import com.soundstore.backend.model.Order;
import com.soundstore.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String CABECERA =
            "Numero Pedido,Fecha,Comprador,Email,Tipo Entrega,Estado,Total COP\n";

    @Transactional(readOnly = true)
    public String generarCsvVentas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin    = hasta.atTime(23, 59, 59);

        List<Order> pedidos = orderRepository.findParaReporte(inicio, fin);

        StringBuilder csv = new StringBuilder(CABECERA);
        for (Order o : pedidos) {
            csv.append(escapar(o.getOrderNumber())).append(',')
               .append(o.getCreatedAt().format(FECHA_FMT)).append(',')
               .append(escapar(o.getUser().getFullName())).append(',')
               .append(escapar(o.getUser().getEmail())).append(',')
               .append(o.getDeliveryType().name()).append(',')
               .append(o.getStatus().name()).append(',')
               .append(o.getTotal().toPlainString()).append('\n');
        }
        return csv.toString();
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
