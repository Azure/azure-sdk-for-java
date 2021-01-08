// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
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


    @Test
    public void loadInitAzureAuthzClient() {
        WebApplicationContextRunnerUtils.getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                "azure.activedirectory.base-uri = fake-uri")
            .run(context -> {
                getBeans(context);

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
            });
    }

    @Test
    public void saveAndLoadAzureAuthzClient() {
        WebApplicationContextRunnerUtils.getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                "azure.activedirectory.base-uri = fake-uri")
            .run(context -> {
                getBeans(context);

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
            });
    }

    private void getBeans(AssertableWebApplicationContext context) {
        AADWebAppClientRegistrationRepository clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
        azure = clientRepo.findByRegistrationId("azure");
        graph = clientRepo.findByRegistrationId("graph");

        authorizedRepo = new AADOAuth2AuthorizedClientRepository(
            clientRepo,
            new JacksonHttpSessionOAuth2AuthorizedClientRepository(),
            OAuth2AuthorizationContext::getAuthorizedClient);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
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
}
