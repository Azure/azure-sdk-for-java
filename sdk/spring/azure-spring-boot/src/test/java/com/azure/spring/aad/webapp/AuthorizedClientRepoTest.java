// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADClientRegistrationRepository;
import com.azure.spring.aad.AADOAuth2AuthorizedClientRepository;
import com.azure.spring.aad.JacksonHttpSessionOAuth2AuthorizedClientRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AuthorizedClientRepoTest {

    @Test
    public void loadInitAzureAuthzClient() {
        try (MockedStatic<RequestContextHolder> requestContextHolder =
                 mockStatic(RequestContextHolder.class, Mockito.CALLS_REAL_METHODS)) {
            WebApplicationContextRunnerUtils
                .getContextRunnerWithRequiredProperties()
                .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
                .withPropertyValues(
                    "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                    "azure.activedirectory.base-uri = fake-uri")
                .run(context -> {
                    AADClientRegistrationRepository clientRepo =
                        context.getBean(AADClientRegistrationRepository.class);
                    InMemoryOAuth2AuthorizedClientService authorizedClientService =
                        context.getBean(InMemoryOAuth2AuthorizedClientService.class);
                    ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                    OAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                        clientRepo,
                        new JacksonHttpSessionOAuth2AuthorizedClientRepository(),
                        OAuth2AuthorizationContext::getAuthorizedClient,
                        authorizedClientService);
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();

                    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                    requestContextHolder.when(() -> RequestContextHolder.currentRequestAttributes())
                                        .thenReturn(attributes);
                    when(attributes.getResponse()).thenReturn(response);

                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(azure),
                        createAuthentication(),
                        request,
                        response);

                    OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
                        "graph",
                        createAuthentication(),
                        request);

                    assertClient(client);
                    assertTrue(isTokenExpired(client.getAccessToken()));
                });
        }
    }

    @Test
    public void saveAndLoadAzureAuthzClient() {
        try (MockedStatic<RequestContextHolder> requestContextHolder =
                 mockStatic(RequestContextHolder.class, Mockito.CALLS_REAL_METHODS)) {
            WebApplicationContextRunnerUtils
                .getContextRunnerWithRequiredProperties()
                .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
                .withPropertyValues(
                    "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                    "azure.activedirectory.base-uri = fake-uri")
                .run(context -> {
                    AADClientRegistrationRepository clientRepo =
                        context.getBean(AADClientRegistrationRepository.class);
                    InMemoryOAuth2AuthorizedClientService authorizedClientService =
                        context.getBean(InMemoryOAuth2AuthorizedClientService.class);
                    ClientRegistration graph = clientRepo.findByRegistrationId("graph");

                    OAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                        clientRepo,
                        new JacksonHttpSessionOAuth2AuthorizedClientRepository(),
                        OAuth2AuthorizationContext::getAuthorizedClient,
                        authorizedClientService);
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                    requestContextHolder.when(() -> RequestContextHolder.currentRequestAttributes())
                                        .thenReturn(attributes);
                    when(attributes.getResponse()).thenReturn(response);

                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(graph),
                        createAuthentication(),
                        request,
                        response);

                    OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
                        "graph",
                        createAuthentication(),
                        request);

                    assertClient(client);
                    assertEquals("fake-access-token", client.getAccessToken().getTokenValue());
                });
        }
    }

    private void assertClient(OAuth2AuthorizedClient client) {
        assertNotNull(client);
        assertNotNull(client.getAccessToken());
        assertNotNull(client.getRefreshToken());
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
        PreAuthenticatedAuthenticationToken authenticationToken =
            new PreAuthenticatedAuthenticationToken("fake-user", "fake-crednetial");
        authenticationToken.setAuthenticated(true);
        return authenticationToken;
    }

    private boolean isTokenExpired(OAuth2AccessToken token) {
        return Optional.ofNullable(token)
            .map(AbstractOAuth2Token::getExpiresAt)
            .map(expiresAt -> expiresAt.isBefore(Instant.now()))
            .orElse(false);
    }
}
