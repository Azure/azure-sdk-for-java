package com.azure.spring.aad.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorizedClientRepoTest {

    private ClientRegistration azure;
    private ClientRegistration graph;

    private OAuth2AuthorizedClientRepository authorizedRepo;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setup() {
        System.setProperty("azure.activedirectory.user-group.allowed-groups", "group1, group2");
        System.setProperty("azure.activedirectory.authorization-server-uri", "fake-uri");
        System.setProperty("azure.activedirectory.tenant-id", "fake-tenant-id");
        System.setProperty("azure.activedirectory.client-id", "fake-client-id");
        System.setProperty("azure.activedirectory.client-secret", "fake-client-secret");
        System.setProperty("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(DumbApp.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        azure = clientRepo.findByRegistrationId("azure");
        graph = clientRepo.findByRegistrationId("graph");

        authorizedRepo = new AzureAuthorizedClientRepository(clientRepo);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void loadInitAzureAuthzClient() {
        authorizedRepo.saveAuthorizedClient(
            createAuthorizedClient(azure),
            createAuthentication(),
            request,
            response);

        OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
            "graph",
            createAuthentication(),
            request);

        assertNotNull(client);
        assertNotNull(client.getAccessToken());
        assertNotNull(client.getRefreshToken());

        assertTrue(isTokenExpired(client.getAccessToken()));
        assertEquals("fake-refresh-token", client.getRefreshToken().getTokenValue());
    }

    @Test
    public void saveAndLoadAzureAuthzClient() {
        authorizedRepo.saveAuthorizedClient(
            createAuthorizedClient(graph),
            createAuthentication(),
            request,
            response);

        OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
            "graph",
            createAuthentication(),
            request);

        assertNotNull(client);
        assertNotNull(client.getAccessToken());
        assertNotNull(client.getRefreshToken());

        assertEquals("fake-access-token", client.getAccessToken().getTokenValue());
        assertEquals("fake-refresh-token", client.getRefreshToken().getTokenValue());
    }

    private OAuth2AuthorizedClient createAuthorizedClient(ClientRegistration client) {
        return new OAuth2AuthorizedClient(
            client,
            "fake-principal-name",
            createAccessToken(),
            createRefreshToken());
    }

    private OAuth2AccessToken createAccessToken() {
        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "fake-access-token",
            Instant.MIN,
            Instant.MAX
        );
    }

    private OAuth2RefreshToken createRefreshToken() {
        return new OAuth2RefreshToken("fake-refresh-token", Instant.MIN);
    }

    private Authentication createAuthentication() {
        return new PreAuthenticatedAuthenticationToken("fake-user", "fake-crednetial");
    }

    private boolean isTokenExpired(OAuth2AccessToken token) {
        return Optional.ofNullable(token)
                       .map(AbstractOAuth2Token::getExpiresAt)
                       .map(expiresAt -> expiresAt.isBefore(Instant.now()))
                       .orElse(false);
    }

    @Configuration
    @SpringBootApplication
    @EnableWebSecurity
    public static class DumbApp {
    }
}
