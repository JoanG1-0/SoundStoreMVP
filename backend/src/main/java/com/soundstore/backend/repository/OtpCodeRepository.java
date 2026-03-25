package com.soundstore.backend.repository;

import com.soundstore.backend.model.OtpCode;
import com.soundstore.backend.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, OtpType type, LocalDateTime now);

    long countByEmailAndTypeAndCreatedAtAfter(String email, OtpType type, LocalDateTime since);
}
