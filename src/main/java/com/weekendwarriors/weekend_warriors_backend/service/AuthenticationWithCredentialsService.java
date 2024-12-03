package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.dto.AuthenticationWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.RefreshTokenRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.RegisterWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.TokenResponse;
import com.weekendwarriors.weekend_warriors_backend.enums.UserRole;
import com.weekendwarriors.weekend_warriors_backend.exception.ExistingUser;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.model.Token;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationWithCredentialsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTBearerService jwtBearerService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterWithCredentialsRequest registerData) throws ExistingUser {
        if (userRepository.findByEmail(registerData.getEmail()).isPresent())
            throw new ExistingUser("Email already in use");
        User user = User.builder()
                .email(registerData.getEmail())
                .password(passwordEncoder.encode(registerData.getPassword()))
                .firstName(registerData.getFirstName())
                .lastName(registerData.getLastName())
                .role(UserRole.BUYER)
                .build();
        userRepository.save(user);
    }

    public TokenResponse login(AuthenticationWithCredentialsRequest loginData) throws UserNotFound {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginData.getEmail(), loginData.getPassword())
            );
        }
        catch (BadCredentialsException e) {
            throw new UserNotFound("Invalid credentials provided");
        }
        User user = userRepository.findByEmail(loginData.getEmail()).orElseThrow(() -> new UserNotFound("Invalid credentials provided"));
        String accessToken = jwtBearerService.generateAccessToken(user.getId());
        String refreshToken = jwtBearerService.generateRefreshToken(user.getId());
        refreshTokenService.saveRefreshTokenForUser(user, refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws InvalidToken {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        try {
            String subject = jwtBearerService.extractSubject(refreshToken);

            User user = userRepository.findById(subject).orElseThrow(() -> new InvalidToken("Invalid token"));

            if (!refreshTokenService.isRefreshTokenValid(refreshToken, subject))
                throw new InvalidToken("Invalid refresh token");

            Token oldRefreshToken = refreshTokenService.findByToken(refreshToken)
                    .orElseThrow(() -> new InvalidToken("Invalid refresh token"));
            refreshTokenService.revokeToken(oldRefreshToken);

            String newAccessToken = jwtBearerService.generateAccessToken(subject);
            String newRefreshToken = jwtBearerService.generateRefreshToken(subject);

            refreshTokenService.saveRefreshTokenForUser(user, newRefreshToken);

            return new TokenResponse(newAccessToken, newRefreshToken);
        }
        catch (ExpiredJwtException exception) {
            throw new InvalidToken("Invalid refresh token");
        }
    }
}
