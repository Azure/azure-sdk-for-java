// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.multitenant;

import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_2;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_2;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADMultipleTenantIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AADMultipleTenantIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @BeforeAll
    public void beforeAll() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        aadSeleniumITHelper = new AADSeleniumITHelper(
            DumbApp.class, properties, AAD_USER_NAME_2, AAD_USER_PASSWORD_2);
        aadSeleniumITHelper.logIn();
    }

    @Test
    public void multipleTenantTest() {
        String httpResponse = aadSeleniumITHelper.httpGet("api/home");
        assertTrue(httpResponse.contains("home"));
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "/api/home")
        public ResponseEntity<String> home(Principal principal) {
            LOGGER.info(((OAuth2AuthenticationToken) principal).getAuthorities().toString());
            return ResponseEntity.ok("home");
        }
    }
}
