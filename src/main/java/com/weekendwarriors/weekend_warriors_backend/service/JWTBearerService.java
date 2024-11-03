package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.enums.JWTTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTBearerService implements IJWTService{
    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${application.security.jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token)
    {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public JWTTokenType extractTokenType(String token) {
        String tokenTypeString = extractClaim(token, claims -> claims.get("type", String.class));
        return JWTTokenType.valueOf(tokenTypeString.toUpperCase());
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public boolean isTokenValid(String token, String subject) {
        final String id = extractSubject(token);
        return id.equals(subject) && !isTokenExpired(token);
    }

    public String generateAccessToken(String subject)
    {
        return createToken(Map.of("type", JWTTokenType.ACCESS), subject, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String subject)
    {
        return createToken(Map.of("type", JWTTokenType.REFRESH), subject, REFRESH_TOKEN_EXPIRATION);
    }

    @Override
    public String createToken(Map<String, Object> extraClaims, String subject, long expiration)
    {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public String createToken(String subject, long expiration)
    {
        return this.createToken(new HashMap<>(), subject, expiration);
    }
}

