package com.soundstore.backend.service;

import com.soundstore.backend.exception.OtpInvalidException;
import com.soundstore.backend.exception.OtpRateLimitException;
import com.soundstore.backend.model.OtpCode;
import com.soundstore.backend.model.OtpType;
import com.soundstore.backend.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private OtpCodeRepository otpCodeRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private JavaMailSender gmailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpCodeRepository, mailSender, gmailSender);
        ReflectionTestUtils.setField(otpService, "fromEmail", "noreply@soundstore.com");
        ReflectionTestUtils.setField(otpService, "gmailFrom", "");
    }

    @Test
    void generateAndSend_exitoso_guardaYEnviaEmail() {
        when(otpCodeRepository.countByEmailAndTypeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        otpService.generateAndSend("test@test.com", OtpType.REGISTRATION);

        ArgumentCaptor<OtpCode> otpCaptor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpCodeRepository).save(otpCaptor.capture());

        OtpCode saved = otpCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getType()).isEqualTo(OtpType.REGISTRATION);
        assertThat(saved.getCode()).hasSize(6);
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void generateAndSend_rateLimitSuperado_lanzaExcepcion() {
        when(otpCodeRepository.countByEmailAndTypeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(3L);

        assertThrows(OtpRateLimitException.class,
                () -> otpService.generateAndSend("test@test.com", OtpType.REGISTRATION));

        verify(otpCodeRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void generateAndSend_codigoTieneSeisDijitos() {
        when(otpCodeRepository.countByEmailAndTypeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        otpService.generateAndSend("test@test.com", OtpType.REGISTRATION);

        ArgumentCaptor<OtpCode> captor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpCodeRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).matches("\\d{6}");
    }

    @Test
    void validate_codigoCorrecto_marcaUsado() {
        OtpCode otp = OtpCode.builder()
                .email("test@test.com")
                .code("123456")
                .type(OtpType.REGISTRATION)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .build();

        when(otpCodeRepository.findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("test@test.com"), eq(OtpType.REGISTRATION), any()))
                .thenReturn(Optional.of(otp));
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(inv -> inv.getArgument(0));

        otpService.validate("test@test.com", "123456", OtpType.REGISTRATION);

        assertThat(otp.isUsed()).isTrue();
        verify(otpCodeRepository).save(otp);
    }

    @Test
    void validate_codigoIncorrecto_lanzaExcepcion() {
        OtpCode otp = OtpCode.builder()
                .email("test@test.com")
                .code("123456")
                .type(OtpType.REGISTRATION)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .build();

        when(otpCodeRepository.findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                anyString(), any(), any()))
                .thenReturn(Optional.of(otp));

        assertThrows(OtpInvalidException.class,
                () -> otpService.validate("test@test.com", "999999", OtpType.REGISTRATION));

        assertThat(otp.isUsed()).isFalse();
    }

    @Test
    void validate_sinCodigoActivo_lanzaExcepcion() {
        when(otpCodeRepository.findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                anyString(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(OtpInvalidException.class,
                () -> otpService.validate("test@test.com", "123456", OtpType.REGISTRATION));
    }
}
