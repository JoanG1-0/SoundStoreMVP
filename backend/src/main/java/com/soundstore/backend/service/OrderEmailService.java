package com.soundstore.backend.service;

import com.soundstore.backend.model.DeliveryType;
import com.soundstore.backend.model.Order;
import com.soundstore.backend.model.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void notificarCambioEstado(Order order) {
        String email      = order.getUser().getEmail();
        String nombre     = order.getUser().getFullName();
        String numeroOrden = order.getOrderNumber();
        OrderStatus status = order.getStatus();
        String tipoEntrega = order.getDeliveryType() == DeliveryType.DELIVERY ? "Domicilio" : "Recogida en tienda";
        String direccion   = order.getDeliveryAddress();
        String total       = formatearPrecio(order.getTotal());

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(construirAsunto(numeroOrden, status));
            helper.setText(
                construirTexto(nombre, numeroOrden, status, tipoEntrega, direccion, total),
                construirHtml(nombre, numeroOrden, status, tipoEntrega, direccion, total)
            );
            mailSender.send(mime);
            log.info("Notificación enviada a {} — pedido: {} estado: {}", email, numeroOrden, status);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo para el pedido {} — {}", numeroOrden, e.getMessage());
        }
    }

    // ── Asuntos ──────────────────────────────────────────────────────────────

    private String construirAsunto(String numeroOrden, OrderStatus status) {
        return switch (status) {
            case PENDING      -> "Tu pedido " + numeroOrden + " fue recibido — SoundStore";
            case CONFIRMED    -> "Tu pedido " + numeroOrden + " fue confirmado — SoundStore";
            case PREPARING    -> "Estamos preparando tu pedido " + numeroOrden + " — SoundStore";
            case ON_THE_WAY   -> "Tu pedido " + numeroOrden + " está en camino — SoundStore";
            case READY_PICKUP -> "Tu pedido " + numeroOrden + " está listo para recoger — SoundStore";
            case DELIVERED    -> "Tu pedido " + numeroOrden + " fue entregado — SoundStore";
            case CANCELLED    -> "Tu pedido " + numeroOrden + " fue cancelado — SoundStore";
        };
    }

    // ── Texto plano ───────────────────────────────────────────────────────────

    private String construirTexto(String nombre, String numeroOrden, OrderStatus status,
                                  String tipoEntrega, String direccion, String total) {
        String intro = introTexto(status, numeroOrden);
        String siguiente = siguienteTexto(status);
        String bloqueInfo = bloqueInfoTexto(numeroOrden, status, tipoEntrega, direccion, total);

        return "Hola, " + nombre + ".\n\n"
            + intro + "\n\n"
            + bloqueInfo + "\n\n"
            + siguiente + "\n\n"
            + "Recibes este mensaje porque realizaste una compra en SoundStore.\n\n"
            + "SoundStore — Colombia\n"
            + "soporte@soundstore.com";
    }

    private String introTexto(OrderStatus status, String numeroOrden) {
        return switch (status) {
            case PENDING ->
                "Recibimos tu pedido en SoundStore y ya está en nuestro sistema. "
                + "Nuestro equipo lo revisará en breve y te confirmará cuando esté listo para procesarse.";
            case CONFIRMED ->
                "Buenas noticias: tu pedido ha sido confirmado. "
                + "Nuestro equipo ya está organizando todo para comenzar a prepararlo.";
            case PREPARING ->
                "Tu pedido está siendo preparado con cuidado por nuestro equipo. "
                + "Estamos asegurándonos de que todo esté en orden antes de enviarlo.";
            case ON_THE_WAY ->
                "Tu pedido ya salió de nuestras instalaciones y está en camino a la dirección que indicaste. "
                + "Te pedimos estar pendiente para recibirlo.";
            case READY_PICKUP ->
                "Tu pedido ya está empacado y listo para ser recogido en nuestra tienda. "
                + "Puedes pasar cuando gustes durante nuestro horario de atención.";
            case DELIVERED ->
                "Tu pedido ha sido entregado exitosamente. "
                + "Esperamos que disfrutes tu música al máximo.";
            case CANCELLED ->
                "Te informamos que tu pedido ha sido cancelado. "
                + "Si tú mismo solicitaste la cancelación, no es necesario que hagas nada más.";
        };
    }

    private String siguienteTexto(OrderStatus status) {
        return switch (status) {
            case PENDING      -> "¿Qué sigue? Recibirás otro correo en cuanto confirmemos tu pedido.";
            case CONFIRMED    -> "Te avisaremos cuando tu pedido esté en preparación.";
            case PREPARING    -> "Cuando tu pedido esté listo, te enviaremos una notificación de inmediato.";
            case ON_THE_WAY   -> "Si necesitas coordinar algo, contáctanos a soporte@soundstore.com.";
            case READY_PICKUP -> "Recuerda presentar el número de pedido al momento de recoger.";
            case DELIVERED    -> "Si tuviste algún inconveniente, escríbenos a soporte@soundstore.com.";
            case CANCELLED    -> "Si NO solicitaste esta cancelación, contáctanos de inmediato a soporte@soundstore.com.";
        };
    }

    private String bloqueInfoTexto(String numeroOrden, OrderStatus status,
                                   String tipoEntrega, String direccion, String total) {
        StringBuilder sb = new StringBuilder();
        sb.append("──────────────────────────\n");
        sb.append("Número de pedido : ").append(numeroOrden).append("\n");
        sb.append("Estado actual    : ").append(etiquetaEstado(status)).append("\n");
        sb.append("Tipo de entrega  : ").append(tipoEntrega).append("\n");
        if (direccion != null && !direccion.isBlank()) {
            sb.append("Dirección        : ").append(direccion).append("\n");
        }
        sb.append("Total            : ").append(total).append("\n");
        sb.append("──────────────────────────");
        return sb.toString();
    }

    // ── HTML ─────────────────────────────────────────────────────────────────

    private String construirHtml(String nombre, String numeroOrden, OrderStatus status,
                                 String tipoEntrega, String direccion, String total) {
        String intro    = introTexto(status, numeroOrden);
        String siguiente = siguienteTexto(status);
        String accentColor = accentColor(status);

        StringBuilder infoRows = new StringBuilder();
        infoRows.append(infoRow("Número de pedido", numeroOrden));
        infoRows.append(infoRow("Estado actual", etiquetaEstado(status)));
        infoRows.append(infoRow("Tipo de entrega", tipoEntrega));
        if (direccion != null && !direccion.isBlank()) {
            infoRows.append(infoRow("Dirección", direccion));
        }
        infoRows.append(infoRow("Total", total));

        return "<!DOCTYPE html>"
            + "<html lang=\"es\">"
            + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>"
            + "<body style=\"margin:0;padding:0;background:#F3F4F6;font-family:Arial,Helvetica,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#F3F4F6;padding:32px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;\">"

            // Header
            + "<tr><td style=\"background:#1E1B4B;padding:28px 40px;border-radius:12px 12px 0 0;\">"
            + "<p style=\"margin:0;font-size:22px;font-weight:700;color:#FFFFFF;letter-spacing:-0.5px;\">SoundStore</p>"
            + "<p style=\"margin:4px 0 0;font-size:12px;color:#A5B4FC;\">Música que se queda contigo</p>"
            + "</td></tr>"

            // Status badge
            + "<tr><td style=\"background:#FFFFFF;padding:24px 40px 0;\">"
            + "<span style=\"display:inline-block;background:" + accentColor + ";color:#FFFFFF;"
            + "font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:1px;"
            + "padding:4px 12px;border-radius:20px;\">"
            + etiquetaEstado(status)
            + "</span>"
            + "</td></tr>"

            // Greeting + intro
            + "<tr><td style=\"background:#FFFFFF;padding:20px 40px;\">"
            + "<p style=\"margin:0 0 12px;font-size:16px;font-weight:700;color:#111827;\">Hola, " + escapeHtml(nombre) + ".</p>"
            + "<p style=\"margin:0;font-size:15px;color:#374151;line-height:1.6;\">" + escapeHtml(intro) + "</p>"
            + "</td></tr>"

            // Info block
            + "<tr><td style=\"background:#FFFFFF;padding:0 40px 24px;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
            + "style=\"background:#F9FAFB;border:1px solid #E5E7EB;border-radius:8px;padding:16px;\">"
            + infoRows
            + "</table>"
            + "</td></tr>"

            // Next step
            + "<tr><td style=\"background:#FFFFFF;padding:0 40px 28px;\">"
            + "<p style=\"margin:0;font-size:14px;color:#6B7280;line-height:1.6;\">"
            + escapeHtml(siguiente)
            + "</p>"
            + "</td></tr>"

            // Footer
            + "<tr><td style=\"background:#F9FAFB;border-top:1px solid #E5E7EB;padding:20px 40px;"
            + "border-radius:0 0 12px 12px;\">"
            + "<p style=\"margin:0 0 4px;font-size:12px;color:#9CA3AF;\">"
            + "Recibes este mensaje porque realizaste una compra en SoundStore."
            + "</p>"
            + "<p style=\"margin:0;font-size:12px;color:#9CA3AF;\">"
            + "SoundStore &mdash; Colombia &nbsp;|&nbsp; "
            + "<a href=\"mailto:soporte@soundstore.com\" style=\"color:#6366F1;text-decoration:none;\">soporte@soundstore.com</a>"
            + "</p>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr>"
            + "</table>"
            + "</body></html>";
    }

    private String infoRow(String label, String value) {
        return "<tr>"
            + "<td style=\"padding:6px 16px;font-size:13px;color:#6B7280;width:45%;\">" + escapeHtml(label) + "</td>"
            + "<td style=\"padding:6px 16px;font-size:13px;color:#111827;font-weight:600;\">" + escapeHtml(value) + "</td>"
            + "</tr>";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String etiquetaEstado(OrderStatus status) {
        return switch (status) {
            case PENDING      -> "Pendiente de confirmación";
            case CONFIRMED    -> "Confirmado";
            case PREPARING    -> "En preparación";
            case ON_THE_WAY   -> "En camino";
            case READY_PICKUP -> "Listo para recoger";
            case DELIVERED    -> "Entregado";
            case CANCELLED    -> "Cancelado";
        };
    }

    private String accentColor(OrderStatus status) {
        return switch (status) {
            case PENDING      -> "#D97706";
            case CONFIRMED    -> "#2563EB";
            case PREPARING    -> "#4F46E5";
            case ON_THE_WAY   -> "#7C3AED";
            case READY_PICKUP -> "#7C3AED";
            case DELIVERED    -> "#059669";
            case CANCELLED    -> "#DC2626";
        };
    }

    private String formatearPrecio(BigDecimal valor) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        fmt.setMaximumFractionDigits(0);
        return fmt.format(valor);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
