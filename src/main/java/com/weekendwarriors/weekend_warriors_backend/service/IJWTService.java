package com.weekendwarriors.weekend_warriors_backend.service;

import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

public interface IJWTService {
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean isTokenExpired(String token);
    boolean isTokenValid(String token, String subject);
    String createToken(Map<String, Object> extraClaims, String subject, long expiration);
}
