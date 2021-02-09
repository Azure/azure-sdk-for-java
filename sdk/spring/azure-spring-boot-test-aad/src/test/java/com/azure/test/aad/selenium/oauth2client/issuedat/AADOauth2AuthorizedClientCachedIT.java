// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.oauth2client.issuedat;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.azure.test.aad.selenium.AADSeleniumITHelper.createDefaultProperties;

public class AADOauth2AuthorizedClientCachedIT {

    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void testOauth2AuthorizedClientCached() {
        Map<String, String> properties = createDefaultProperties();
        properties.put(
            "azure.activedirectory.authorization-clients.office.scopes",
            "https://manage.office.com/ActivityFeed.Read, "
                + "https://manage.office.com/ActivityFeed.ReadDlp, "
                + "https://manage.office.com/ServiceHealth.Read");
        properties.put(
            "azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read, https://graph.microsoft.com/Directory.Read.All");

        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();

        // If Oauth2AuthorizedClient is cached, the issuedAt value should be equal.
        Assert.assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/azure"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/azure"));

        Assert.assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/graph"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/graph"));

        Assert.assertEquals(
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/office"),
            aadSeleniumITHelper.httpGet("accessTokenIssuedAt/office"));
    }

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
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

        @GetMapping(value = "accessTokenIssuedAt/office")
        public String office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .map(Instant::toString)
                           .orElse(null);
        }
    }

}
