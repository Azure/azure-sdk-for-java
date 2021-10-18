package com.azure.spring.security.oauth2.client;

import com.azure.spring.security.oauth2.client.implementation.OnBehalfOfHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.net.MalformedURLException;
import java.time.Instant;

import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_1;
import static com.azure.spring.security.oauth2.client.utils.ClientRegistrations.CLIENT_REGISTRATION_OBO;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnBehalfOfOAuth2AuthorizedClientProviderTest {

    @Test
    public void testNotTargetAuthorizationGrantType() {
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        BearerTokenAuthentication principal = mock(BearerTokenAuthentication.class);
        when(principal.getToken()).thenReturn(oAuth2AccessToken);
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_1,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient)
                                      .principal(principal)
                                      .build();
        OnBehalfOfOAuth2AuthorizedClientProvider provider = new OnBehalfOfOAuth2AuthorizedClientProvider();
        assertNull(provider.authorize(context));
    }

    @Test
    public void testAccessTokenNotExpired() {
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now(),
            Instant.now().plusSeconds(60 * 60));
        BearerTokenAuthentication principal = mock(BearerTokenAuthentication.class);
        when(principal.getToken()).thenReturn(oAuth2AccessToken);
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_OBO,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient)
                                      .principal(principal)
                                      .build();
        OnBehalfOfOAuth2AuthorizedClientProvider provider = new OnBehalfOfOAuth2AuthorizedClientProvider();
        assertNull(provider.authorize(context));
    }

    @Test
    public void testAnonymousAuthenticationToken() {
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now().minusSeconds(60 * 60),
            Instant.now());
        Authentication principal = mock(AnonymousAuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_OBO,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient)
                                      .principal(principal)
                                      .build();
        OnBehalfOfOAuth2AuthorizedClientProvider provider = new OnBehalfOfOAuth2AuthorizedClientProvider();
        assertNull(provider.authorize(context));
    }

    @Test
    public void testOnBehalfOfToken() {
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "tokenValue",
            Instant.now().minusSeconds(60 * 60),
            Instant.now());
        BearerTokenAuthentication principal = mock(BearerTokenAuthentication.class);
        when(principal.getToken()).thenReturn(oAuth2AccessToken);
        when(principal.getName()).thenReturn("mockName");
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            CLIENT_REGISTRATION_OBO,
            "principalName",
            oAuth2AccessToken);
        OAuth2AuthorizationContext context =
            OAuth2AuthorizationContext.withAuthorizedClient(authorizedClient)
                                      .principal(principal)
                                      .build();
        OnBehalfOfOAuth2AuthorizedClientProvider provider = new OnBehalfOfOAuth2AuthorizedClientProvider();
        // accessToken copied from: https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
        String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Imk2bEdrM0ZaenhSY1ViMkMzbkVRN3N5SEpsWSJ9"
            + ".eyJhdWQiOiI2ZTc0MTcyYi1iZTU2LTQ4NDMtOWZmNC1lNjZhMzliYjEyZTMiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc2"
            + "9mdG9ubGluZS5jb20vNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3L3YyLjAiLCJpYXQiOjE1MzcyMzEwNDgsI"
            + "m5iZiI6MTUzNzIzMTA0OCwiZXhwIjoxNTM3MjM0OTQ4LCJhaW8iOiJBWFFBaS84SUFBQUF0QWFaTG8zQ2hNaWY2S09udHRSQjdl"
            + "QnE0L0RjY1F6amNKR3hQWXkvQzNqRGFOR3hYZDZ3TklJVkdSZ2hOUm53SjFsT2NBbk5aY2p2a295ckZ4Q3R0djMzMTQwUmlvT0Z"
            + "KNGJDQ0dWdW9DYWcxdU9UVDIyMjIyZ0h3TFBZUS91Zjc5UVgrMEtJaWpkcm1wNjlSY3R6bVE9PSIsImF6cCI6IjZlNzQxNzJiLW"
            + "JlNTYtNDg0My05ZmY0LWU2NmEzOWJiMTJlMyIsImF6cGFjciI6IjAiLCJuYW1lIjoiQWJlIExpbmNvbG4iLCJvaWQiOiI2OTAyM"
            + "jJiZS1mZjFhLTRkNTYtYWJkMS03ZTRmN2QzOGU0NzQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhYmVsaUBtaWNyb3NvZnQuY29t"
            + "IiwicmgiOiJJIiwic2NwIjoiYWNjZXNzX2FzX3VzZXIiLCJzdWIiOiJIS1pwZmFIeVdhZGVPb3VZbGl0anJJLUtmZlRtMjIyWDV"
            + "yclYzeERxZktRIiwidGlkIjoiNzJmOTg4YmYtODZmMS00MWFmLTkxYWItMmQ3Y2QwMTFkYjQ3IiwidXRpIjoiZnFpQnFYTFBqMG"
            + "VRYTgyUy1JWUZBQSIsInZlciI6IjIuMCJ9.pj4N-w_3Us9DrBLfpCt";
        try (MockedStatic<OnBehalfOfHttpClient> utilities = Mockito.mockStatic(OnBehalfOfHttpClient.class)) {
            utilities.when(() -> OnBehalfOfHttpClient.getOnBehalfOfAccessToken(any(), any(), any(), any(), any()))
                     .thenReturn(accessToken);
            Assertions.assertNotNull(provider.authorize(context));
        }
    }

    @Test
    public void toAuthorityTest() throws MalformedURLException {
        // Refs: https://docs.microsoft.com/en-us/azure/active-directory/develop/authentication-national-cloud

        // Azure AD (global service)
        Assertions.assertEquals("https://login.microsoftonline.com/common/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.microsoftonline.com/common/oauth2/v2.0/authorize"));
        Assertions.assertEquals("https://login.microsoftonline.com/tenant-id/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.microsoftonline.com/tenant-id/oauth2/v2.0/authorize"));

        // Azure AD China operated by 21Vianet
        Assertions.assertEquals("https://login.partner.microsoftonline.cn/common/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.partner.microsoftonline.cn/common/oauth2/v2.0/authorize"));
        Assertions.assertEquals("https://login.partner.microsoftonline.cn/tenant-id/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.partner.microsoftonline.cn/tenant-id/oauth2/v2.0/authorize"));

        // Azure AD for US Government
        Assertions.assertEquals("https://login.microsoftonline.us/common/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.microsoftonline.us/common/oauth2/v2.0/authorize"));
        Assertions.assertEquals("https://login.microsoftonline.us/tenant-id/",
            OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://login.microsoftonline.us/tenant-id/oauth2/v2.0/authorize"));

        // Invalid uri
        assertThrows(MalformedURLException.class,
            () -> OnBehalfOfOAuth2AuthorizedClientProvider.toAuthority("https://www.google.com"));
    }
}
