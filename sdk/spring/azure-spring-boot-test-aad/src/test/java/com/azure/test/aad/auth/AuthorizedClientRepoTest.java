package com.azure.test.aad.auth;

import java.time.Instant;

import com.azure.spring.aad.implementation.AzureAuthorizedClientRepository;
import com.azure.spring.aad.implementation.AzureClientRegistrationRepository;
import com.azure.test.utils.AppRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorizedClientRepoTest {

    private AppRunner runner;

    private ClientRegistration azure;
    private ClientRegistration graph;

    private OAuth2AuthorizedClientRepository repo;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setup() {
        runner = createApp();
        runner.start();

        AzureClientRegistrationRepository clientRepo = runner.getBean(AzureClientRegistrationRepository.class);
        azure = clientRepo.findByRegistrationId("azure");
        graph = clientRepo.findByRegistrationId("graph");

        repo = new AzureAuthorizedClientRepository(clientRepo);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(AzureActiveDirectoryConfigurationTest.DumbApp.class);
        result.property("azure.active.directory.uri", "fake-uri");
        result.property("azure.active.directory.tenant-id", "fake-tenant-id");
        result.property("azure.active.directory.client-id", "fake-client-id");
        result.property("azure.active.directory.client-secret", "fake-client-secret");
        result.property("azure.active.directory.authorization.graph.scopes", "Calendars.Read");
        return result;
    }

    @AfterEach
    public void tearDown() {
        runner.stop();
    }

    @Test
    public void loadInitAzureAuthzClient() {
        repo.saveAuthorizedClient(
            createAuthorizedClient(azure),
            createAuthentication(),
            request,
            response);

        OAuth2AuthorizedClient client = repo.loadAuthorizedClient(
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
        repo.saveAuthorizedClient(
            createAuthorizedClient(graph),
            createAuthentication(),
            request,
            response);

        OAuth2AuthorizedClient client = repo.loadAuthorizedClient(
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
        OAuth2AuthorizedClient result = new OAuth2AuthorizedClient(
            client,
            "fake-principal-name",
            createAccessToken(),
            createRefreshToken());

        return result;
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
        return token.getExpiresAt().isBefore(Instant.now());
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableWebSecurity
    public static class DumbApp {}
}
