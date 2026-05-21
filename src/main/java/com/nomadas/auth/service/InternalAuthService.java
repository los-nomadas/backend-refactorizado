package com.nomadas.auth.service;

import com.nomadas.security.filter.dto.LoginRequest;
import com.nomadas.security.filter.dto.LoginResponse;

public interface InternalAuthService {
    LoginResponse login(LoginRequest loginRequest);
}
