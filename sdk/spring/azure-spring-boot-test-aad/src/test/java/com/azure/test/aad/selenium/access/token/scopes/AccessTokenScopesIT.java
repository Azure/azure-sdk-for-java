
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.access.token.scopes;

import com.azure.test.aad.selenium.SeleniumTestUtils;
import com.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

public class AccessTokenScopesIT {

    @Test
    public void testAccessTokenScopes() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            SeleniumTestUtils.addProperty(app);
            app.property("azure.activedirectory.authorization-clients.office.scopes", "https://manage.office.com/ActivityFeed.Read , https://manage.office.com/ActivityFeed.ReadDlp , https://manage.office.com/ServiceHealth.Read");
            app.property("azure.activedirectory.authorization-clients.graph.scopes", "https://graph.microsoft.com/User.Read , https://graph.microsoft.com/Directory.AccessAsUser.All");
            List<String> endPoints = new ArrayList<>();
            endPoints.add("accessTokenScopes/azure");
            endPoints.add("accessTokenScopes/office");
            endPoints.add("accessTokenScopes/graph");
            endPoints.add("accessTokenScopes/arm");
            Map<String, String> result = SeleniumTestUtils.get(app, endPoints);

            Assert.assertFalse(result.get("accessTokenScopes/office").contains("profile"));
            Assert.assertTrue(result.get("accessTokenScopes/office").contains("https://manage.office.com/ActivityFeed.Read"));
            Assert.assertTrue(result.get("accessTokenScopes/office").contains("https://manage.office.com/ActivityFeed.ReadDlp"));
            Assert.assertTrue(result.get("accessTokenScopes/office").contains("https://manage.office.com/ServiceHealth.Read"));

            Assert.assertTrue(result.get("accessTokenScopes/azure").contains("profile"));
            Assert.assertTrue(result.get("accessTokenScopes/azure").contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
            Assert.assertTrue(result.get("accessTokenScopes/azure").contains("https://graph.microsoft.com/User.Read"));

            Assert.assertTrue(result.get("accessTokenScopes/graph").contains("profile"));
            Assert.assertTrue(result.get("accessTokenScopes/graph").contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
            Assert.assertTrue(result.get("accessTokenScopes/graph").contains("https://graph.microsoft.com/User.Read"));

            Assert.assertNotEquals("error", result.get("api/arm"));
        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "accessTokenScopes/office")
        public Set<String> office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getScopes)
                .orElse(null);
        }

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
        public String arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return "error";
        }
    }

}
