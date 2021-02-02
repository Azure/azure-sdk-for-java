// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.ondemand;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_ON_DEMAND;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_ON_DEMAND;
import static com.azure.test.aad.selenium.AADSeleniumITHelper.createDefaultProperties;

public class AADOnDemandIT {
    private AADSeleniumITHelper aadSeleniumITHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AADOnDemandIT.class);

    @Test
    public void onDemandTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.authorization-clients.arm.scopes",
            "https://management.azure.com/user_impersonation");
        properties.put("azure.activedirectory.authorization-clients.arm.on-demand", "true");
        LOGGER.info(AAD_USER_NAME_ON_DEMAND);

        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties,
            AAD_USER_NAME_ON_DEMAND, AAD_USER_PASSWORD_ON_DEMAND);
        aadSeleniumITHelper.logIn();

        String httpResponse = aadSeleniumITHelper.httpGet("api/azure");
        Assert.assertTrue(httpResponse.contains("azure"));

        httpResponse = aadSeleniumITHelper.httpGetWithIncreamentalConsent("api/arm");
        Assert.assertTrue(httpResponse.contains("arm"));
    }

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "/api/azure")
        public ResponseEntity<String> azure(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
            return ResponseEntity.ok("azure");
        }

        @GetMapping(value = "/api/arm")
        public ResponseEntity<String> arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return ResponseEntity.ok("arm");
        }
    }
}