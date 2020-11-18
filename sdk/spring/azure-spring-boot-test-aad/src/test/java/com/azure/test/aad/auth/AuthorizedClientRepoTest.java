package com.azure.test.aad.auth;

import com.azure.spring.autoconfigure.aad.AzureClientRegistrationRepository;
import com.azure.spring.autoconfigure.aad.AzureOAuth2AuthorizedClientRepository;
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

    private AppRunner appRunner;

    private ClientRegistration azureClientRegistration;
    private ClientRegistration graphClientRegistration;

    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    private MockHttpServletRequest mockHttpServletRequest;
    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    public void setup() {
        appRunner = createApp();
        appRunner.start();

        AzureClientRegistrationRepository azureClientRegistrationRepository =
            appRunner.getBean(AzureClientRegistrationRepository.class);
        azureClientRegistration = azureClientRegistrationRepository.findByRegistrationId("azure");
        graphClientRegistration = azureClientRegistrationRepository.findByRegistrationId("graph");

        oAuth2AuthorizedClientRepository = new AzureOAuth2AuthorizedClientRepository(azureClientRegistrationRepository);
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(AppAutoConfigTest.DumbApp.class);
        result.property("azure.activedirectory.tenant-id", "fake-tenant-id");
        result.property("azure.activedirectory.client-id", "fake-client-id");
        result.property("azure.activedirectory.client-secret", "fake-client-secret");
        result.property("azure.activedirectory.user-group.allowed-groups", "group1");
        result.property("azure.activedirectory.authorization.graph.scope", "Calendars.Read");
        return result;
    }

    @AfterEach
    public void tearDown() {
        appRunner.stop();
    }

    @Test
    public void loadInitAzureAuthzClient() {
        oAuth2AuthorizedClientRepository.saveAuthorizedClient(
            toOAuthAuthorizedClient(azureClientRegistration),
            createAuthentication(),
            mockHttpServletRequest,
            mockHttpServletResponse
        );

        OAuth2AuthorizedClient oAuth2AuthorizedClient =
            oAuth2AuthorizedClientRepository.loadAuthorizedClient(
                "graph",
                createAuthentication(),
                mockHttpServletRequest
            );

        assertNotNull(oAuth2AuthorizedClient);
        assertNotNull(oAuth2AuthorizedClient.getAccessToken());
        assertNotNull(oAuth2AuthorizedClient.getRefreshToken());

        assertTrue(isTokenExpired(oAuth2AuthorizedClient.getAccessToken()));
        assertEquals("fake-refresh-token", oAuth2AuthorizedClient.getRefreshToken().getTokenValue());
    }

    @Test
    public void saveAndLoadAzureAuthzClient() {
        oAuth2AuthorizedClientRepository.saveAuthorizedClient(
            toOAuthAuthorizedClient(graphClientRegistration),
            createAuthentication(),
            mockHttpServletRequest,
            mockHttpServletResponse
        );

        OAuth2AuthorizedClient oAuth2AuthorizedClient =
            oAuth2AuthorizedClientRepository.loadAuthorizedClient(
                "graph",
                createAuthentication(),
                mockHttpServletRequest
            );

        assertNotNull(oAuth2AuthorizedClient);
        assertNotNull(oAuth2AuthorizedClient.getAccessToken());
        assertNotNull(oAuth2AuthorizedClient.getRefreshToken());

        assertEquals("fake-access-token", oAuth2AuthorizedClient.getAccessToken().getTokenValue());
        assertEquals("fake-refresh-token", oAuth2AuthorizedClient.getRefreshToken().getTokenValue());
    }

    private OAuth2AuthorizedClient toOAuthAuthorizedClient(ClientRegistration clientRegistration) {
        return new OAuth2AuthorizedClient(
            clientRegistration,
            "fake-principal-name",
            createAccessToken(),
            createRefreshToken()
        );
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

    private boolean isTokenExpired(OAuth2AccessToken oAuth2AccessToken) {
        return Optional.ofNullable(oAuth2AccessToken)
                       .map(AbstractOAuth2Token::getExpiresAt)
                       .map(expiredAt -> expiredAt.isBefore(Instant.now()))
                       .orElse(false);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableWebSecurity
    public static class DumbApp {
    }
}
