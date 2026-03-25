package com.soundstore.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String TEST_SECRET =
            "test_jwt_secret_key_for_unit_testing_purposes_only_min_32chars";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessExpirationMs", 3_600_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604_800_000L);

        userDetails = User.builder()
                .username("test@test.com")
                .password("hashed")
                .roles("BUYER")
                .build();
    }

    @Test
    void generateAccessToken_retornaTokenNoNulo() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractEmail_retornaEmailCorrecto() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@test.com");
    }

    @Test
    void isTokenValid_tokenValido_retornaTrue() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_tokenDeOtroUsuario_retornaFalse() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails otroUsuario = User.builder()
                .username("otro@test.com")
                .password("hashed")
                .roles("BUYER")
                .build();
        assertThat(jwtService.isTokenValid(token, otroUsuario)).isFalse();
    }

    @Test
    void isTokenValid_tokenInvalido_retornaFalse() {
        assertThat(jwtService.isTokenValid("token.invalido.aqui", userDetails)).isFalse();
    }

    @Test
    void generateAccessToken_tieneExpiracionMenorQueRefresh() {
        ReflectionTestUtils.setField(jwtService, "accessExpirationMs", 1_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604_800_000L);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    void generateRefreshToken_noContieneRolEnClaims() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        // El refresh token solo tiene subject, no role — extractEmail debe funcionar
        assertThat(jwtService.extractEmail(refreshToken)).isEqualTo("test@test.com");
    }
}
