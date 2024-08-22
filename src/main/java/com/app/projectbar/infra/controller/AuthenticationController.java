package com.app.projectbar.infra.controller;

import com.app.projectbar.application.impl.AuthenticationService;
import com.app.projectbar.application.impl.JwtService;
import com.app.projectbar.domain.User;
import com.app.projectbar.domain.UserDetailsForSecurity;
import com.app.projectbar.domain.responses.LoginResponse;
import com.app.projectbar.domain.securitydtos.LoginUserDto;
import com.app.projectbar.domain.securitydtos.RegisterUserDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    private final ModelMapper modelMapper;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, ModelMapper modelMapper) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto){
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto){

        User authenticateUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticateUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();


        return ResponseEntity.ok(loginResponse);
    }



}
