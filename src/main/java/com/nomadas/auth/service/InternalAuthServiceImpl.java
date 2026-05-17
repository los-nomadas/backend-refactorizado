package com.nomadas.auth.service;

import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.entity.InternalCredential;
import com.nomadas.exception.UnauthorizedException;
import com.nomadas.security.JwtProvider;
import com.nomadas.security.filter.dto.LoginRequest;
import com.nomadas.security.filter.dto.LoginResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Transactional(readOnly = true)
public class InternalAuthServiceImpl implements InternalAuthService {

    private final InternalCredentialRepository internalCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public InternalAuthServiceImpl(
            InternalCredentialRepository internalCredentialRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.internalCredentialRepository = internalCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        InternalCredential credential = internalCredentialRepository.findByUsername(loginRequest.getUsername())
                .filter(InternalCredential::getActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), credential.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(
                credential.getId(),
                credential.getUsername(),
                credential.getRole().name()
        );

        return LoginResponse.of(
                token,
                credential.getId(),
                credential.getUsername(),
                credential.getEmail(),
                credential.getRole().name(),
                toLocalDateTime(jwtProvider.getExpirationDateFromToken(token))
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withNano(0);
    }
}
