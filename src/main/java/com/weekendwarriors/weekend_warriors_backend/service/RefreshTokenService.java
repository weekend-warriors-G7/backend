package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.enums.JWTTokenType;
import com.weekendwarriors.weekend_warriors_backend.model.Token;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final TokenRepository tokenRepository;
    private final JWTBearerService jwtBearerService;

    public void saveRefreshTokenForUser(User user, String refreshToken) {
        var token = Token.builder()
                .user(user)
                .token(refreshToken)
                .type(JWTTokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void revokeToken(Token token) {
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public boolean isRefreshTokenValid(String refreshToken, String subject) {
        var token = tokenRepository.findByToken(refreshToken);
        if(token.isPresent()) {
            if(token.get().getType() == JWTTokenType.REFRESH && !token.get().isExpired() && !token.get().isRevoked())
                return jwtBearerService.isTokenValid(refreshToken, subject);
        }
        return false;
    }
}
