// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.oauth2client.issuedat;

import com.azure.spring.utils.AzureCloudUrls;
import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADOauth2AuthorizedClientCachedIT {

    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void testOauth2AuthorizedClientCached() {
        Map<String, String> properties = createDefaultProperties();

        String armClientUrl = AzureCloudUrls.getServiceManagementBaseUrl(AZURE_CLOUD_TYPE);
        String armClientScope = armClientUrl + "user_impersonation";
        properties.put("azure.activedirectory.authorization-clients.arm.scopes", armClientScope);

        String graphBaseUrl = AzureCloudUrls.getGraphBaseUrl(AZURE_CLOUD_TYPE);
        properties.put("azure.activedirectory.authorization-clients.graph.scopes",
            graphBaseUrl + "User.Read, " + graphBaseUrl + "Directory.Read.All");
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();

        // If Oauth2AuthorizedClient is cached, the issuedAt value should be equal.
        assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/azure"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/azure"));

        assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/graph"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/graph"));

        assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/arm"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/arm"));
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "accessTokenIssuedAt/azure")
        public String azure(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .map(Instant::toString)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenIssuedAt/graph")
        public String graph(
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .map(Instant::toString)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenIssuedAt/arm")
        public String arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .map(Instant::toString)
                           .orElse(null);
        }
    }

}
