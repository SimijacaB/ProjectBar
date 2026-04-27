package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.securityDtos.LoginRequestDTO;
import com.app.projectbar.domain.securityDtos.LoginResponseDTO;
import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.infra.repositories.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(IUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();

        UserEntity user = userRepository.findById(username)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: usuario '{}' no encontrado", username);
                    return new RuntimeException("Invalid username or password");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Intento de login fallido: contraseña incorrecta para usuario '{}'", username);
            throw new RuntimeException("Invalid username or password");
        }

        // Validar que la cuenta no esté bloqueada o deshabilitada
        if (user.getLocked()) {
            log.warn("Intento de login fallido: cuenta '{}' bloqueada", username);
            throw new RuntimeException("Account is locked");
        }

        if (user.getDisabled()) {
            log.warn("Intento de login fallido: cuenta '{}' deshabilitada", username);
            throw new RuntimeException("Account is disabled");
        }

        log.info("Login exitoso para usuario '{}'", username);
        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponseDTO(token);
    }
}
