package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.securityDtos.LoginRequestDTO;
import com.app.projectbar.domain.securityDtos.LoginResponseDTO;
import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.infra.repositories.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(IUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponseDTO(token);
    }
}
