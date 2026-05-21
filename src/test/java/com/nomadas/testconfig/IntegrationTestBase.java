package com.nomadas.testconfig;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected static final String API_BASE = "/api";
    protected static final String HEADER_AUTHORIZATION = "Authorization";
    protected static final String BEARER_PREFIX = "Bearer ";

    protected String createAuthHeader(String token) {
        return BEARER_PREFIX + token;
    }
}
