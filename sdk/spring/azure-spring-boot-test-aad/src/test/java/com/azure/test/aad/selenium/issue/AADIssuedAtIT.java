package com.azure.test.aad.selenium.issue;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import com.azure.test.aad.selenium.access.token.scopes.AADAccessTokenScopesIT;
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

public class AADIssuedAtIT {

    private AADSeleniumITHelper aadSeleniumITHelper;

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @Test
    public void issuedAtTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put(
            "azure.activedirectory.authorization-clients.office.scopes",
            "https://manage.office.com/ActivityFeed.Read, https://manage.office.com/ActivityFeed.ReadDlp, "
                + "https://manage.office.com/ServiceHealth.Read");
        properties.put(
            "azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read, https://graph.microsoft.com/Directory.AccessAsUser.All");
        aadSeleniumITHelper = new AADSeleniumITHelper(AADAccessTokenScopesIT.DumbApp.class, properties);
        aadSeleniumITHelper.logIn();
        String httpResponse1 = aadSeleniumITHelper.httpGet("accessTokenScopes/azure");
        String httpResponse2 = aadSeleniumITHelper.httpGet("accessTokenScopes/azure");
        Assert.assertEquals(httpResponse1, httpResponse2);

        httpResponse1 = aadSeleniumITHelper.httpGet("accessTokenScopes/graph");
        httpResponse2 = aadSeleniumITHelper.httpGet("accessTokenScopes/graph");
        Assert.assertEquals(httpResponse1, httpResponse2);

        httpResponse1 = aadSeleniumITHelper.httpGet("accessTokenScopes/office");
        httpResponse2 = aadSeleniumITHelper.httpGet("accessTokenScopes/office");
        Assert.assertEquals(httpResponse1, httpResponse2);
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "accessTokenScopes/azure")
        public Instant azure(
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenScopes/graph")
        public Instant graph(
            @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .orElse(null);
        }

        @GetMapping(value = "accessTokenScopes/office")
        public Instant office(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                           .map(OAuth2AuthorizedClient::getAccessToken)
                           .map(OAuth2AccessToken::getIssuedAt)
                           .orElse(null);
        }
    }
}
