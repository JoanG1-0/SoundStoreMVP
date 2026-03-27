package com.soundstore.backend.service;

import com.soundstore.backend.exception.OtpInvalidException;
import com.soundstore.backend.exception.OtpRateLimitException;
import com.soundstore.backend.model.OtpCode;
import com.soundstore.backend.model.OtpType;
import com.soundstore.backend.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int RATE_LIMIT_MAX = 3;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

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
            log.warn("No se pudo enviar el correo OTP a {} — {}. El código fue guardado.", email, e.getMessage());
        }
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

    private void sendEmail(String to, String code, OtpType type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);

        if (type == OtpType.REGISTRATION) {
            message.setSubject("Verifica tu cuenta - SoundStore");
            message.setText(
                    "Hola,\n\n" +
                    "Tu código de verificación es: " + code + "\n\n" +
                    "Este código es válido por 5 minutos y solo puede usarse una vez.\n" +
                    "Si no solicitaste este código, ignora este mensaje.\n\n" +
                    "Equipo SoundStore");
        } else {
            message.setSubject("Recuperación de contraseña - SoundStore");
            message.setText(
                    "Hola,\n\n" +
                    "Tu código de recuperación de contraseña es: " + code + "\n\n" +
                    "Este código es válido por 5 minutos y solo puede usarse una vez.\n" +
                    "Si no solicitaste este código, ignora este mensaje.\n\n" +
                    "Equipo SoundStore");
        }

        mailSender.send(message);
    }
}
