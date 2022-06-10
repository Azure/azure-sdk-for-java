// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.time.Instant;

import static com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AadAzureDelegatedOAuth2AuthorizedClientProviderTests {

    private static final ClientRegistration AZURE_CLIENT_REGISTRATION =
        toClientRegistrationBuilder(AZURE_CLIENT_REGISTRATION_ID)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .build();

    private static final ClientRegistration DELEGATED_CLIENT_REGISTRATION =
        toClientRegistrationBuilder("delegated")
            .authorizationGrantType(new AuthorizationGrantType(AadAuthorizationGrantType.AZURE_DELEGATED.getValue()))
            .scope("testScope")
            .build();

    private static final ClientRegistration CLIENT_CREDENTIALS_CLIENT_REGISTRATION =
        toClientRegistrationBuilder("clientCredentials")
            .authorizationGrantType(new AuthorizationGrantType(AadAuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
            .build();

    private static ClientRegistration.Builder toClientRegistrationBuilder(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientId("clientId")
                                 .clientSecret("clientSecret")
                                 .redirectUri("redirectUri")
                                 .authorizationUri("authorizationUri")
                                 .tokenUri("tokenUri");
    }

    @Test
    void testGrantTypeIsNotAzureDelegated() {
        AadAzureDelegatedOAuth2AuthorizedClientProvider provider =
            new AadAzureDelegatedOAuth2AuthorizedClientProvider(null, null);
        Authentication principal = mock(Authentication.class);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withClientRegistration(AZURE_CLIENT_REGISTRATION)
                                      .principal(principal)
                                      .build();
        assertNull(provider.authorize(context));
        context = OAuth2AuthorizationContext.withClientRegistration(CLIENT_CREDENTIALS_CLIENT_REGISTRATION)
                                            .principal(principal)
                                            .build();
        assertNull(provider.authorize(context));
    }

    @Test
    void testDelegatedClientNotExpired() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        OAuth2AuthorizedClient delegatedAuthorizedClient = new OAuth2AuthorizedClient(
            DELEGATED_CLIENT_REGISTRATION,
            "principalName",
            oAuth2AccessToken);
        Authentication principal = mock(Authentication.class);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(delegatedAuthorizedClient)
                                      .principal(principal)
                                      .build();
        AadAzureDelegatedOAuth2AuthorizedClientProvider provider =
            new AadAzureDelegatedOAuth2AuthorizedClientProvider(null, authorizedClientRepository);
        assertNull(provider.authorize(context));
    }

    @Test
    void testAzureClientIsNull() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        when(authorizedClientRepository.loadAuthorizedClient(any(), any(), any())).thenReturn(null);
        Authentication principal = mock(Authentication.class);
        AadAzureDelegatedOAuth2AuthorizedClientProvider provider =
            new AadAzureDelegatedOAuth2AuthorizedClientProvider(null, authorizedClientRepository);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withClientRegistration(DELEGATED_CLIENT_REGISTRATION)
                                      .principal(principal)
                                      .build();
        assertThrows(ClientAuthorizationRequiredException.class, () -> provider.authorize(context));
    }

    @Test
    void testGetAccessTokenByRefreshToken() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now().minusSeconds(60 * 60),
            Instant.now());
        OAuth2RefreshToken oAuth2RefreshToken = new OAuth2RefreshToken(
            "fakeTokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        OAuth2AuthorizedClient azureAuthorizedClient = new OAuth2AuthorizedClient(
            AZURE_CLIENT_REGISTRATION,
            "principalName",
            oAuth2AccessToken,
            oAuth2RefreshToken);
        OAuth2AuthorizedClient delegatedAuthorizedClient = new OAuth2AuthorizedClient(
            DELEGATED_CLIENT_REGISTRATION,
            "principalName",
            oAuth2AccessToken);
        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn("principalName");
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(delegatedAuthorizedClient)
                                      .principal(principal)
                                      .build();
        when(authorizedClientRepository.loadAuthorizedClient(any(), any(), any())).thenReturn(azureAuthorizedClient);
        RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider =
            mock(RefreshTokenOAuth2AuthorizedClientProvider.class);
        OAuth2AuthorizedClient clientGetByRefreshToken = new OAuth2AuthorizedClient(
            ClientRegistration.withClientRegistration(DELEGATED_CLIENT_REGISTRATION)
                              .scope("testScope")
                              .build(),
            "principalName1",
            oAuth2AccessToken);
        when(refreshTokenProvider.authorize(any())).thenReturn(clientGetByRefreshToken);
        AadAzureDelegatedOAuth2AuthorizedClientProvider provider =
            new AadAzureDelegatedOAuth2AuthorizedClientProvider(refreshTokenProvider, authorizedClientRepository);
        assertEquals(clientGetByRefreshToken, provider.authorize(context));
    }
}
