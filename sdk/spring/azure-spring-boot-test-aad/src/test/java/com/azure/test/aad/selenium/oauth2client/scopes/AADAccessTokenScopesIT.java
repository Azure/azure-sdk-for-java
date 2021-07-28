// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.oauth2client.scopes;

import com.azure.spring.utils.AzureCloudUrls;
import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADAccessTokenScopesIT {

    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void testAccessTokenScopes() {
        Map<String, String> properties = createDefaultProperties();
        String armClientUrl = AzureCloudUrls.getServiceManagementBaseUrl(AZURE_CLOUD_TYPE);
        String armClientScope = armClientUrl + "user_impersonation";
        properties.put("azure.activedirectory.authorization-clients.arm.scopes", armClientScope);
        String graphBaseUrl = AzureCloudUrls.getGraphBaseUrl(AZURE_CLOUD_TYPE);
        properties.put("azure.activedirectory.authorization-clients.graph.scopes",
            graphBaseUrl + "User.Read, " + graphBaseUrl + "Directory.Read.All");

        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();

        String httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/azure");
        Assertions.assertTrue(httpResponse.contains("profile"));
        Assertions.assertTrue(httpResponse.contains("Directory.Read.All"));
        Assertions.assertTrue(httpResponse.contains("User.Read"));

        httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/graph");
        Assertions.assertTrue(httpResponse.contains("profile"));
        Assertions.assertTrue(httpResponse.contains("Directory.Read.All"));
        Assertions.assertTrue(httpResponse.contains("User.Read"));

        httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/arm");
        Assertions.assertFalse(httpResponse.contains("profile"));
        Assertions.assertTrue(httpResponse.contains("user_impersonation"));

        httpResponse = aadSeleniumITHelper.httpGet("notExist");
        Assertions.assertNotEquals(httpResponse, "notExist");
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "accessTokenScopes/azure")
        public Set<String> azure(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenScopes/graph")
        public Set<String> graph(
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenScopes/arm")
        public Set<String> arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "notExist")
        public String notExist(
            @RegisteredOAuth2AuthorizedClient("notExist") OAuth2AuthorizedClient authorizedClient) {
            return "notExist";
        }
    }

}
