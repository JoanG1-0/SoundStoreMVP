package com.soundstore.backend.service;

import com.soundstore.backend.model.Order;
import com.soundstore.backend.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void notificarCambioEstado(Order order) {
        String email = order.getUser().getEmail();
        String nombre = order.getUser().getFullName();
        String numeroOrden = order.getOrderNumber();
        OrderStatus status = order.getStatus();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(construirAsunto(numeroOrden, status));
        message.setText(construirCuerpo(nombre, numeroOrden, status));

        try {
            mailSender.send(message);
            log.info("Notificación de pedido enviada a {} — pedido: {} estado: {}", email, numeroOrden, status);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de notificación para el pedido {} — {}", numeroOrden, e.getMessage());
        }
    }

    private String construirAsunto(String numeroOrden, OrderStatus status) {
        return switch (status) {
            case PENDING      -> "Pedido recibido — " + numeroOrden + " | SoundStore";
            case CONFIRMED    -> "Pedido confirmado — " + numeroOrden + " | SoundStore";
            case PREPARING    -> "Pedido en preparación — " + numeroOrden + " | SoundStore";
            case ON_THE_WAY   -> "Pedido en camino — " + numeroOrden + " | SoundStore";
            case READY_PICKUP -> "Pedido listo para recoger — " + numeroOrden + " | SoundStore";
            case DELIVERED    -> "Pedido entregado — " + numeroOrden + " | SoundStore";
            case CANCELLED    -> "Pedido cancelado — " + numeroOrden + " | SoundStore";
        };
    }

    private String construirCuerpo(String nombre, String numeroOrden, OrderStatus status) {
        String saludo = "Hola, " + nombre + ".\n\n";
        String despedida = "\n\nGracias por confiar en SoundStore.\nEquipo SoundStore";

        String detalle = switch (status) {
            case PENDING ->
                    "Hemos recibido tu pedido " + numeroOrden + " correctamente.\n" +
                    "Está pendiente de confirmación por parte de nuestro equipo.";
            case CONFIRMED ->
                    "Tu pedido " + numeroOrden + " ha sido confirmado.\n" +
                    "Pronto comenzaremos a prepararlo.";
            case PREPARING ->
                    "Tu pedido " + numeroOrden + " está siendo preparado.\n" +
                    "Te avisaremos cuando esté listo.";
            case ON_THE_WAY ->
                    "Tu pedido " + numeroOrden + " está en camino.\n" +
                    "Pronto llegará a la dirección de entrega indicada.";
            case READY_PICKUP ->
                    "Tu pedido " + numeroOrden + " está listo para ser recogido.\n" +
                    "Puedes pasar por nuestra tienda cuando gustes.";
            case DELIVERED ->
                    "Tu pedido " + numeroOrden + " ha sido entregado exitosamente.\n" +
                    "¡Esperamos que disfrutes tu música!";
            case CANCELLED ->
                    "Tu pedido " + numeroOrden + " ha sido cancelado.\n" +
                    "Si no solicitaste esta cancelación, contáctanos de inmediato.";
        };

        return saludo + detalle + despedida;
    }
}
