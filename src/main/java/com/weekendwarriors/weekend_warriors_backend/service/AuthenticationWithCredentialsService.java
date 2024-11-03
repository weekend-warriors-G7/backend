package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.dto.AuthenticationWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.TokenResponse;
import com.weekendwarriors.weekend_warriors_backend.enums.JWTTokenType;
import com.weekendwarriors.weekend_warriors_backend.enums.UserRole;
import com.weekendwarriors.weekend_warriors_backend.exception.ExistingUser;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.model.Token;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.TokenRepository;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
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
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTBearerService jwtBearerService;
    private final AuthenticationManager authenticationManager;

    public void register(AuthenticationWithCredentialsRequest registerData) throws ExistingUser {
        if (userRepository.findByEmail(registerData.getEmail()).isPresent())
            throw new ExistingUser("Email already in use");
        User user = User.builder()
                .email(registerData.getEmail())
                .password(passwordEncoder.encode(registerData.getPassword()))
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
        saveRefreshTokenForUser(user, refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }

    private void saveRefreshTokenForUser(User user, String refreshToken) {
        var token = Token.builder()
                .user(user)
                .token(refreshToken)
                .type(JWTTokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
