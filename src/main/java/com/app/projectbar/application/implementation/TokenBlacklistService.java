package com.app.projectbar.application.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para manejar la blacklist de tokens JWT revocados.
 * Permite implementar logout efectivo revocando tokens antes de su expiración natural.
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    // Mapa de tokens revocados: token -> fecha de expiración del token
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Agrega un token a la blacklist
     */
    public void addToBlacklist(String token, Instant expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        log.info("Token agregado a blacklist - expira en: {}", expirationTime);
    }

    /**
     * Verifica si un token está en la blacklist
     */
    public boolean isBlacklisted(String token) {
        // Limpiar tokens expirados periódicamente
        cleanExpiredTokens();

        return blacklistedTokens.containsKey(token);
    }

    /**
     * Limpia tokens expirados de la blacklist
     */
    private void cleanExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}