// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * A strategy for authorizing (or re-authorizing) an OAuth 2.0 Client. This implementations implement {@link
 * AADAuthorizationGrantType "azure_delegated" authorization grant type}.
 *
 * @author RujunChen
 * @see OAuth2AuthorizedClient
 * @see OAuth2AuthorizationContext
 * @see AADAuthorizationGrantType
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3">Section 1.3 Authorization Grant</a>
 * @since 3.8.0
 */
public class AADAzureDelegatedOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private final Clock clock;
    private final Duration clockSkew;
    private final OAuth2AuthorizedClientProvider provider;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public AADAzureDelegatedOAuth2AuthorizedClientProvider(
        RefreshTokenOAuth2AuthorizedClientProvider provider,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.clock = Clock.systemUTC();
        this.clockSkew = Duration.ofSeconds(60);
        this.provider = provider;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");
        ClientRegistration clientRegistration = context.getClientRegistration();
        if (!AADAuthorizationGrantType.AZURE_DELEGATED.isSameGrantType(
            clientRegistration.getAuthorizationGrantType())) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if (authorizedClient != null && tokenNotExpired(authorizedClient.getAccessToken())) {
            return null;
        }
        Authentication principal = context.getPrincipal();
        OAuth2AuthorizedClient azureClient = authorizedClientRepository.loadAuthorizedClient(
            AZURE_CLIENT_REGISTRATION_ID,
            principal,
            getHttpServletRequestOrDefault(context));
        if (azureClient == null) {
            throw new ClientAuthorizationRequiredException(AZURE_CLIENT_REGISTRATION_ID);
        }
        OAuth2AuthorizedClient clientWithExpiredToken =
            createClientWithExpiredToken(azureClient, clientRegistration, principal);
        String[] scopes = clientRegistration.getScopes().toArray(new String[0]);
        OAuth2AuthorizationContext refreshTokenAuthorizationContext =
            OAuth2AuthorizationContext.withAuthorizedClient(clientWithExpiredToken)
                                      .principal(principal)
                                      .attributes(attributes -> attributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME, scopes))
                                      .build();
        return provider.authorize(refreshTokenAuthorizationContext);
    }

    private boolean tokenNotExpired(OAuth2Token token) {
        return Optional.ofNullable(token)
                       .map(OAuth2Token::getExpiresAt)
                       .map(expiredAt -> this.clock.instant().isBefore(expiredAt.minus(this.clockSkew)))
                       .orElse(false);
    }

    private OAuth2AuthorizedClient createClientWithExpiredToken(OAuth2AuthorizedClient azureClient,
                                                                ClientRegistration clientRegistration,
                                                                Authentication principal) {
        Assert.notNull(azureClient, "azureClient cannot be null");
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "non-access-token",
            Instant.MIN,
            Instant.now().minus(100, ChronoUnit.DAYS));
        return new OAuth2AuthorizedClient(
            clientRegistration,
            principal.getName(),
            accessToken,
            azureClient.getRefreshToken()
        );
    }

    private static HttpServletRequest getHttpServletRequestOrDefault(OAuth2AuthorizationContext context) {
        return Optional.ofNullable(context)
                       .map(OAuth2AuthorizationContext::getAttributes)
                       .map(attributes -> (HttpServletRequest) attributes.get(HttpServletRequest.class.getName()))
                       .orElseGet(AADAzureDelegatedOAuth2AuthorizedClientProvider::getDefaultHttpServletRequest);
    }

    private static HttpServletRequest getDefaultHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                       .filter(attributes -> attributes instanceof ServletRequestAttributes)
                       .map(attributes -> ((ServletRequestAttributes) attributes).getRequest())
                       .orElse(null);
    }
}
