package com.nomadas.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected static final String API_BASE = "/api";
    protected static final String DEMO_TOKEN = "demo-token-for-tests";
    protected static final String ADMIN_TOKEN = "demo-admin-token";

    protected String extractJsonPath(MvcResult result, String path) throws Exception {
        return result.getResponse().getContentAsString();
    }

    protected void validateErrorResponse(MvcResult result, int expectedStatus) throws Exception {
        String content = result.getResponse().getContentAsString();
        assert result.getResponse().getStatus() == expectedStatus;
        assert content.contains("status");
        assert content.contains("error");
        assert content.contains("message");
        assert content.contains("path");
        assert content.contains("timestamp");
    }

    protected void validateCommonFields(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assert !content.isEmpty();
    }
}
