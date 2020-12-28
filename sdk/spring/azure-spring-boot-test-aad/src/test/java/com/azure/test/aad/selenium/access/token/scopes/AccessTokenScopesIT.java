// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.access.token.scopes;

import com.azure.test.aad.selenium.AADLoginRunner;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

public class AccessTokenScopesIT {

    @Test
    public void testAccessTokenScopes() {
        AADLoginRunner.build(DumbApp.class).extendsConfigure(app -> {

            app.property("azure.activedirectory.authorization-clients.office.scopes",
                "https://manage.office.com/ActivityFeed.Read , "
                    + "https://manage.office.com/ActivityFeed.ReadDlp , "
                    + "https://manage.office.com/ServiceHealth.Read");
            app.property("azure.activedirectory.authorization-clients.graph.scopes",
                "https://graph.microsoft.com/User.Read , "
                    + "https://graph.microsoft.com/Directory.AccessAsUser.All");

        }).login().run((app, driver) -> {

            driver.get((app.root() + "accessTokenScopes/azure"));
            Thread.sleep(1000);
            String result = driver.findElement(By.tagName("body")).getText();
            Assert.assertTrue(result.contains("profile"));
            Assert.assertTrue(result.contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
            Assert.assertTrue(result.contains("https://graph.microsoft.com/User.Read"));

            driver.get((app.root() + "accessTokenScopes/office"));
            Thread.sleep(1000);
            result = driver.findElement(By.tagName("body")).getText();
            Assert.assertFalse(result.contains("profile"));
            Assert.assertTrue(result.contains("https://manage.office.com/ActivityFeed.Read"));
            Assert.assertTrue(result.contains("https://manage.office.com/ActivityFeed.ReadDlp"));
            Assert.assertTrue(result.contains("https://manage.office.com/ServiceHealth.Read"));

            driver.get((app.root() + "accessTokenScopes/graph"));
            Thread.sleep(1000);
            result = driver.findElement(By.tagName("body")).getText();
            Assert.assertTrue(result.contains("profile"));
            Assert.assertTrue(result.contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
            Assert.assertTrue(result.contains("https://graph.microsoft.com/User.Read"));

            driver.get((app.root() + "accessTokenScopes/arm"));
            Thread.sleep(1000);
            result = driver.findElement(By.tagName("body")).getText();
            Assert.assertNotEquals("error", result);

        });
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "accessTokenScopes/arm")
        public String arm(
            @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient authorizedClient) {
            return "error";
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

        @GetMapping(value = "accessTokenScopes/office")
        public Set<String> office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getScopes)
                           .orElse(null);
        }
    }

}
