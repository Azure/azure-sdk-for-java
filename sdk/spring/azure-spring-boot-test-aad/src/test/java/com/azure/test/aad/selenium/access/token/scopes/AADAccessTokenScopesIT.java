// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.access.token.scopes;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AADAccessTokenScopesIT {

    @Test
    public void testAccessTokenScopes() throws InterruptedException {
        Map<String, String> arguments = new HashMap<>();
        arguments.put(
            "azure.activedirectory.authorization-clients.office.scopes",
            "https://manage.office.com/ActivityFeed.Read, https://manage.office.com/ActivityFeed.ReadDlp, "
                + "https://manage.office.com/ServiceHealth.Read");
        arguments.put(
            "azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read, https://graph.microsoft.com/Directory.AccessAsUser.All");
        AADSeleniumITHelper aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, arguments);

        String httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/azure");
        Assert.assertTrue(httpResponse.contains("profile"));
        Assert.assertTrue(httpResponse.contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
        Assert.assertTrue(httpResponse.contains("https://graph.microsoft.com/User.Read"));

        httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/graph");
        Assert.assertTrue(httpResponse.contains("profile"));
        Assert.assertTrue(httpResponse.contains("https://graph.microsoft.com/Directory.AccessAsUser.All"));
        Assert.assertTrue(httpResponse.contains("https://graph.microsoft.com/User.Read"));

        httpResponse = aadSeleniumITHelper.httpGet("accessTokenScopes/office");
        Assert.assertFalse(httpResponse.contains("profile"));
        Assert.assertTrue(httpResponse.contains("https://manage.office.com/ActivityFeed.Read"));
        Assert.assertTrue(httpResponse.contains("https://manage.office.com/ActivityFeed.ReadDlp"));
        Assert.assertTrue(httpResponse.contains("https://manage.office.com/ServiceHealth.Read"));

        httpResponse = aadSeleniumITHelper.httpGet("notExist");
        Assert.assertNotEquals(httpResponse, "notExist");
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
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

        @GetMapping(value = "accessTokenScopes/office")
        public Set<String> office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
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
