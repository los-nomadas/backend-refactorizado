package com.nomadas.auth.controller;

import com.nomadas.auth.service.InternalAuthService;
import com.nomadas.security.filter.dto.LoginRequest;
import com.nomadas.security.filter.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final InternalAuthService internalAuthService;

    public AuthController(InternalAuthService internalAuthService) {
        this.internalAuthService = internalAuthService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return internalAuthService.login(loginRequest);
    }
}
