// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.refresh.token;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RefreshTokenScopesIT {

    @Test
    public void testRefreshTokenConverter() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            SeleniumTestUtils.addProperty(app);
            app.property("azure.activedirectory.authorization.office.scopes",
                "https://manage.office.com/ActivityFeed.Read");
            app.property("azure.activedirectory.authorization.graph.scopes", "https://graph.microsoft.com/User.Read");
            List<String> endPoints = new ArrayList<>();
            endPoints.add("api/office");
            endPoints.add("api/azure");
            endPoints.add("api/graph");
            endPoints.add("api/arm");
            Map<String, String> result = SeleniumTestUtils.get(app, endPoints);

            Assert.assertFalse(result.get("api/office").contains("profile"));
            Assert.assertTrue(result.get("api/office").contains("https://manage.office.com/ActivityFeed.Read"));

            Assert.assertTrue(result.get("api/azure").contains("profile"));
            Assert.assertTrue(result.get("api/azure").contains("https://graph.microsoft.com/User.Read"));

            Assert.assertTrue(result.get("api/graph").contains("profile"));
            Assert.assertTrue(result.get("api/graph").contains("https://graph.microsoft.com/User.Read"));

            Assert.assertNotEquals("error", result.get("api/arm"));
        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "api/office")
        public Set<String> office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "api/azure")
        public Set<String> azure(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "api/graph")
        public Set<String> graph(
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }

        @GetMapping(value = "api/arm")
        public String arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return "error";
        }
    }

}
