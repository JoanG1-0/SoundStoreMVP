package com.soundstore.backend.config;

import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@soundstore.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin1234*}")
    private String adminPassword;

    @Value("${app.admin.full-name:Administrador SoundStore}")
    private String adminFullName;

    @Value("${app.admin.phone:3001234567}")
    private String adminPhone;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .fullName(adminFullName)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .phone(adminPhone)
                    .role(UserRole.ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin inicial creado: {}", adminEmail);
        } else {
            log.info("Admin inicial ya existe: {}", adminEmail);
        }
    }
}
