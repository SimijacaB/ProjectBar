package com.app.projectbar.config;

import com.app.projectbar.application.implementation.JwtUtil;
import com.app.projectbar.application.implementation.TokenBlacklistService;
import com.app.projectbar.application.implementation.UserServiceSecurity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserServiceSecurity userServiceSecurity;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserServiceSecurity userServiceSecurity,
                               TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userServiceSecurity = userServiceSecurity;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        boolean tokenExpired = false;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            // Verificar si el token está en la blacklist (logout)
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Intento de uso de token revocdo: {}", request.getRequestURI());
                response.setHeader("X-Token-Revoked", "true");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                username = jwtUtil.extractUsername(token);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                tokenExpired = true;
                // Continuar sin autenticar - Spring Security manejará el acceso no autorizado
            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT token: {}", e.getMessage());
            } catch (SignatureException e) {
                log.warn("Invalid JWT signature: {}", e.getMessage());
            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                log.warn("JWT claims string is empty: {}", e.getMessage());
            }
        }

        // Si el token está expirado, agregar un header para informar al cliente
        if (tokenExpired) {
            response.setHeader("X-Token-Expired", "true");
            log.info("Token expired - client should refresh or re-authenticate");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userServiceSecurity.loadUserByUsername(username);
                if (jwtUtil.isTokenValid(token, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User '{}' authenticated successfully", username);
                }
            } catch (Exception e) {
                log.error("Error during authentication for user '{}': {}", username, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
