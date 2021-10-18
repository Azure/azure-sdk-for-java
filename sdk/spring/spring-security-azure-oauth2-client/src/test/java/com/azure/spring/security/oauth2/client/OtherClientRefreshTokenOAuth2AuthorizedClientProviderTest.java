package com.azure.spring.security.oauth2.client;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.time.Instant;

import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_1;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_2;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_3;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_ID_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OtherClientRefreshTokenOAuth2AuthorizedClientProviderTest {

    @Test
    public void testNotTargetAuthorizationGrantType() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        Authentication principal = mock(Authentication.class);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        OAuth2AuthorizedClient authorizedClient3 = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_3,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient3)
                                      .principal(principal)
                                      .build();
        OtherClientRefreshTokenOAuth2AuthorizedClientProvider provider =
            new OtherClientRefreshTokenOAuth2AuthorizedClientProvider(CLIENT_REGISTRATION_ID_1,
                authorizedClientRepository);
        assertNull(provider.authorize(context));
    }

    @Test
    public void testClient2NotExpired() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        Authentication principal = mock(Authentication.class);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        OAuth2AuthorizedClient authorizedClient2 = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_2,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient2)
                                      .principal(principal)
                                      .build();
        OtherClientRefreshTokenOAuth2AuthorizedClientProvider provider =
            new OtherClientRefreshTokenOAuth2AuthorizedClientProvider(CLIENT_REGISTRATION_ID_1,
                authorizedClientRepository);
        assertNull(provider.authorize(context));
    }

    @Test
    public void testAzureClient1IsNull() {
        OAuth2AuthorizedClientRepository authorizedClientRepository = mock(OAuth2AuthorizedClientRepository.class);
        when(authorizedClientRepository.loadAuthorizedClient(any(), any(), any())).thenReturn(null);
        Authentication principal = mock(Authentication.class);
        OtherClientRefreshTokenOAuth2AuthorizedClientProvider provider =
            new OtherClientRefreshTokenOAuth2AuthorizedClientProvider(CLIENT_REGISTRATION_ID_1,
                authorizedClientRepository);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withClientRegistration(CLIENT_REGISTRATION_2)
                                      .principal(principal)
                                      .build();
        assertThrows(ClientAuthorizationRequiredException.class, () -> provider.authorize(context));
    }

    @Test
    public void testGetAccessTokenByRefreshToken() {
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
        OAuth2AuthorizedClient authorizedClient1 = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_1,
            "principalName",
            oAuth2AccessToken,
            oAuth2RefreshToken);
        OAuth2AuthorizedClient authorizedClient2 = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_2,
            "principalName",
            oAuth2AccessToken);
        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn("principalName");
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient2)
                                      .principal(principal)
                                      .build();
        when(authorizedClientRepository.loadAuthorizedClient(any(), any(), any())).thenReturn(authorizedClient1);
        RefreshTokenOAuth2AuthorizedClientProvider refreshTokenProvider =
            mock(RefreshTokenOAuth2AuthorizedClientProvider.class);
        OAuth2AuthorizedClient clientGetByRefreshToken = new OAuth2AuthorizedClient(
            ClientRegistration.withClientRegistration(CLIENT_REGISTRATION_2)
                              .scope("testScope")
                              .build(),
            "principalName1",
            oAuth2AccessToken);
        when(refreshTokenProvider.authorize(any())).thenReturn(clientGetByRefreshToken);
        OtherClientRefreshTokenOAuth2AuthorizedClientProvider provider =
            new OtherClientRefreshTokenOAuth2AuthorizedClientProvider(
                CLIENT_REGISTRATION_ID_1, authorizedClientRepository, refreshTokenProvider);
        assertEquals(clientGetByRefreshToken, provider.authorize(context));
    }
}
