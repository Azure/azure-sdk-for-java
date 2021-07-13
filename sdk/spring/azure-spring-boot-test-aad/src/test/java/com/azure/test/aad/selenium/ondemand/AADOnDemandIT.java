// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.ondemand;

import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_ON_DEMAND;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_ON_DEMAND;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADOnDemandIT {
    private AADSeleniumITHelper aadSeleniumITHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AADOnDemandIT.class);

    @Test
    public void onDemandTest() {
        String armClientUrl = AADSeleniumITHelper.getServiceManagementBaseUrl(AZURE_CLOUD_TYPE);
        String armClientScope = armClientUrl + "user_impersonation";
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.authorization-clients.arm.scopes", armClientScope);
        properties.put("azure.activedirectory.authorization-clients.arm.on-demand", "true");

        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties,
            AAD_USER_NAME_ON_DEMAND, AAD_USER_PASSWORD_ON_DEMAND);
        aadSeleniumITHelper.logIn();

        String httpResponse = aadSeleniumITHelper.httpGet("api/azure");
        Assertions.assertTrue(httpResponse.contains("azure"));

        String incrementalConsentUrl = aadSeleniumITHelper.httpGetWithIncrementalConsent("api/arm");
        Assertions.assertTrue(incrementalConsentUrl.contains(armClientScope));

        httpResponse = aadSeleniumITHelper.httpGet("api/arm");
        LOGGER.info("onDemandTest, httpResponse = {}", httpResponse);
        Assertions.assertTrue(httpResponse.contains("arm"));
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

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
