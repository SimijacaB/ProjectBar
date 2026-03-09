package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.securityDtos.LoginRequestDTO;
import com.app.projectbar.domain.securityDtos.LoginResponseDTO;

public interface IAuthService {
    LoginResponseDTO authenticate(LoginRequestDTO loginRequest);
}
