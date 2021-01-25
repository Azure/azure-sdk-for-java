// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.multitenant;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.font.FontRenderContext;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_2;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_2;

public class AADMultipleTenantIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AADMultipleTenantIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void multipleTenantTest() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        String[] clientIdArray = {AAD_MULTI_TENANT_CLIENT_ID, AAD_B2C_CLIENT_ID, AAD_SINGLE_TENANT_CLIENT_ID,
            AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE};
        for (String clientID : clientIdArray) {
            LOGGER.info(clientID);
        }
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties,
            AAD_USER_NAME_2, AAD_USER_PASSWORD_2);
        aadSeleniumITHelper.logIn();

        String httpResponse = aadSeleniumITHelper.httpGet("api/home");
        Assert.assertTrue(httpResponse.contains("home"));
    }

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
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
