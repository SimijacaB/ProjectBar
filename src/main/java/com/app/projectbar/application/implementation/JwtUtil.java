package com.app.projectbar.application.implementation;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtUtil(
            @Value("${security.jwt.secret-key}") String secretKeyString,
            @Value("${security.jwt.expiration-time}") long expirationTime
    ) {
        // Validar que la clave JWT esté configurada - NO hay valor por defecto
        if (secretKeyString == null || secretKeyString.isBlank()) {
            throw new IllegalStateException("JWT_SECRET_KEY no está configurada. "
                    + "Debe establecer la variable de entorno JWT_SECRET_KEY con una clave de al menos 256 bits.");
        }

        // La clave debe tener al menos 256 bits (32 bytes) para HS256
        if (secretKeyString.length() < 32) {
            throw new IllegalStateException("JWT_SECRET_KEY debe tener al menos 32 caracteres (256 bits) para HS256. "
                    + "Clave actual: " + secretKeyString.length() + " caracteres.");
        }

        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
        log.info("JWT configurado correctamente con clave de {} bits", secretKeyString.length() * 8);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Obtiene la fecha de expiración de un token
     */
    public Instant getExpirationTime(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.toInstant();
    }
}
