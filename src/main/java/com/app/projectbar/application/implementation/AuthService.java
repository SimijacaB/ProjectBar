package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.auth.InvalidCredentialsException;
import com.app.projectbar.application.interfaces.IAuthService;
import com.app.projectbar.domain.securityDtos.LoginRequestDTO;
import com.app.projectbar.domain.securityDtos.LoginResponseDTO;
import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.infra.repositories.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(IUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();
        String username = loginRequest.getUsername();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: email '{}' no encontrado", email);
                    return new InvalidCredentialsException(
                            ErrorMessagesService.INVALID_CREDENTIALS.getMessage());
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Intento de login fallido: contraseña incorrecta para usuario '{}'", username);
            throw new InvalidCredentialsException(ErrorMessagesService.INVALID_CREDENTIALS.getMessage());
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
