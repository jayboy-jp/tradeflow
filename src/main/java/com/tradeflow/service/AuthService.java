package com.tradeflow.service;

import com.tradeflow.dto.AuthTokensResponse;
import com.tradeflow.entity.RefreshToken;
import com.tradeflow.entity.User;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.RefreshTokenRepository;
import com.tradeflow.repository.UserRepository;
import com.tradeflow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${app.refresh-token.expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthTokensResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        String refreshToken = issueRefreshToken(user);
        return AuthTokensResponse.of(accessToken, refreshToken, jwtExpirationMs / 1000);
    }

    public AuthTokensResponse refreshAccessToken(String providedRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(providedRefreshToken)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        String newRefreshToken = issueRefreshToken(user);
        return AuthTokensResponse.of(newAccessToken, newRefreshToken, jwtExpirationMs / 1000);
    }

    public void logout(String providedRefreshToken) {
        if (providedRefreshToken == null || providedRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(providedRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String issueRefreshToken(User user) {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken(
                user,
                token,
                Instant.now().plusMillis(refreshTokenExpirationMs)
        );
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
