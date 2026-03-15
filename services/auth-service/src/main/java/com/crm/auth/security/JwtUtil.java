package com.crm.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpiry;
    private final long refreshExpiry;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms}") long accessExpiry,
            @Value("${jwt.refresh-token-expiry-ms}") long refreshExpiry) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiry = accessExpiry;
        this.refreshExpiry = refreshExpiry;
    }

    public String generateAccessToken(String email, List<String> roles) {
        return buildToken(email, roles, accessExpiry, "access");
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, List.of(), refreshExpiry, "refresh");
    }

    private String buildToken(String subject, List<String> roles, long expiry, String type) {
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaims(token).get("type", String.class));
    }
}
