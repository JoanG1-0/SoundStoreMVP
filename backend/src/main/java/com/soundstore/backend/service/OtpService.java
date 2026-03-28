package com.soundstore.backend.service;

import com.soundstore.backend.exception.OtpInvalidException;
import com.soundstore.backend.exception.OtpRateLimitException;
import com.soundstore.backend.model.OtpCode;
import com.soundstore.backend.model.OtpType;
import com.soundstore.backend.repository.OtpCodeRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class OtpService {

    private static final int OTP_EXPIRATION_MINUTES    = 5;
    private static final int RATE_LIMIT_MAX            = 3;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM    = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;
    private final JavaMailSender gmailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.gmail.username:}")
    private String gmailFrom;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      JavaMailSender mailSender,
                      @Qualifier("gmailSender") JavaMailSender gmailSender) {
        this.otpCodeRepository = otpCodeRepository;
        this.mailSender        = mailSender;
        this.gmailSender       = gmailSender;
    }

    @Transactional
    public void generateAndSend(String email, OtpType type) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES);
        long recentCount = otpCodeRepository.countByEmailAndTypeAndCreatedAtAfter(email, type, windowStart);

        if (recentCount >= RATE_LIMIT_MAX) {
            throw new OtpRateLimitException(
                    "Ha superado el límite de intentos. Por favor intente de nuevo en 10 minutos.");
        }

        String code = generateCode();

        OtpCode otp = OtpCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .build();

        otpCodeRepository.save(otp);

        try {
            sendEmail(email, code, type);
            log.info("OTP generado y enviado a: {} tipo: {}", email, type);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo OTP vía SendGrid a {} — {}. El código fue guardado.", email, e.getMessage());
        }
        sendEmailGmail(email, code, type);
    }

    @Transactional
    public void validate(String email, String code, OtpType type) {
        OtpCode otp = otpCodeRepository
                .findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        email, type, LocalDateTime.now())
                .orElseThrow(() -> new OtpInvalidException(
                        "El código OTP no es válido o ha expirado."));

        if (!otp.getCode().equals(code)) {
            throw new OtpInvalidException("El código OTP no es válido o ha expirado.");
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
        log.info("OTP validado correctamente para: {} tipo: {}", email, type);
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void sendEmail(String to, String code, OtpType type) throws Exception {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(construirAsunto(code, type));
        helper.setText(construirTexto(code, type), construirHtml(code, type));
        mailSender.send(mime);
    }

    private void sendEmailGmail(String to, String code, OtpType type) {
        if (gmailFrom == null || gmailFrom.isBlank()) return;
        try {
            MimeMessage mime = gmailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(gmailFrom);
            helper.setTo(to);
            helper.setSubject(construirAsunto(code, type));
            helper.setText(construirTexto(code, type), construirHtml(code, type));
            gmailSender.send(mime);
            log.info("OTP enviado vía Gmail a: {}", to);
        } catch (Exception e) {
            log.warn("No se pudo enviar el OTP vía Gmail a {} — {}", to, e.getMessage());
        }
    }

    // ── Asuntos ───────────────────────────────────────────────────────────────

    private String construirAsunto(String code, OtpType type) {
        return switch (type) {
            case REGISTRATION   -> code + " es tu código de verificación — SoundStore";
            case PASSWORD_RESET -> code + " es tu código para restablecer tu contraseña — SoundStore";
        };
    }

    // ── Texto plano ───────────────────────────────────────────────────────────

    private String construirTexto(String code, OtpType type) {
        String intro, motivo;

        if (type == OtpType.REGISTRATION) {
            intro  = "Recibimos una solicitud para verificar tu cuenta en SoundStore.";
            motivo = "Recibes este correo porque alguien registró esta dirección en SoundStore.";
        } else {
            intro  = "Recibimos una solicitud para restablecer la contraseña de tu cuenta en SoundStore.";
            motivo = "Recibes este correo porque se solicitó un restablecimiento de contraseña para esta dirección en SoundStore.";
        }

        String advertencia = type == OtpType.REGISTRATION
            ? "Si no creaste una cuenta en SoundStore, puedes ignorar este mensaje."
            : "Si no solicitaste este cambio, puedes ignorar este mensaje; tu contraseña no será modificada.";

        return "Hola,\n\n"
            + intro + "\n\n"
            + "Tu código es:\n\n"
            + "  " + code + "\n\n"
            + "Este código es válido por 5 minutos y solo puede usarse una vez.\n"
            + advertencia + "\n\n"
            + motivo + "\n\n"
            + "SoundStore — Colombia\n"
            + "soporte@soundstore.com";
    }

    // ── HTML ──────────────────────────────────────────────────────────────────

    private String construirHtml(String code, OtpType type) {
        String titulo, intro, advertencia, motivo;

        if (type == OtpType.REGISTRATION) {
            titulo      = "Verifica tu cuenta";
            intro       = "Recibimos una solicitud para verificar tu cuenta en SoundStore. "
                        + "Usa el código de abajo para completar tu registro.";
            advertencia = "Si no creaste una cuenta en SoundStore, puedes ignorar este mensaje.";
            motivo      = "Recibes este correo porque alguien registró esta dirección en SoundStore.";
        } else {
            titulo      = "Restablece tu contraseña";
            intro       = "Recibimos una solicitud para restablecer la contraseña de tu cuenta en SoundStore. "
                        + "Usa el código de abajo para continuar.";
            advertencia = "Si no solicitaste este cambio, puedes ignorar este mensaje. Tu contraseña no será modificada.";
            motivo      = "Recibes este correo porque se solicitó un restablecimiento de contraseña para esta dirección en SoundStore.";
        }

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

            // Title + intro
            + "<tr><td style=\"background:#FFFFFF;padding:32px 40px 20px;\">"
            + "<p style=\"margin:0 0 12px;font-size:20px;font-weight:700;color:#111827;\">" + titulo + "</p>"
            + "<p style=\"margin:0;font-size:15px;color:#374151;line-height:1.6;\">" + intro + "</p>"
            + "</td></tr>"

            // Code block
            + "<tr><td style=\"background:#FFFFFF;padding:0 40px 24px;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td align=\"center\" style=\"background:#F0F0FF;border:2px dashed #6366F1;"
            + "border-radius:10px;padding:24px;\">"
            + "<p style=\"margin:0 0 6px;font-size:12px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;\">Tu código</p>"
            + "<p style=\"margin:0;font-size:40px;font-weight:700;color:#4F46E5;letter-spacing:10px;\">" + code + "</p>"
            + "<p style=\"margin:8px 0 0;font-size:12px;color:#9CA3AF;\">Válido por 5 minutos &middot; Uso único</p>"
            + "</td></tr>"
            + "</table>"
            + "</td></tr>"

            // Warning
            + "<tr><td style=\"background:#FFFFFF;padding:0 40px 28px;\">"
            + "<p style=\"margin:0;font-size:13px;color:#9CA3AF;line-height:1.6;\">" + advertencia + "</p>"
            + "</td></tr>"

            // Footer
            + "<tr><td style=\"background:#F9FAFB;border-top:1px solid #E5E7EB;padding:20px 40px;"
            + "border-radius:0 0 12px 12px;\">"
            + "<p style=\"margin:0 0 4px;font-size:12px;color:#9CA3AF;\">" + motivo + "</p>"
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
}
