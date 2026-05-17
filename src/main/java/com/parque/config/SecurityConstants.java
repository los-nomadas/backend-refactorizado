package com.parque.config;

public class SecurityConstants {

    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/**",
        "/error"
    };

    public static final String[] PUBLIC_READ_ENDPOINTS = {
        "/api/hotels",
        "/api/hotels/*"
    };

    public static final String[] USER_REGISTRATION_ENDPOINTS = {
        "/api/users"
    };

    public static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/**"
    };
}
