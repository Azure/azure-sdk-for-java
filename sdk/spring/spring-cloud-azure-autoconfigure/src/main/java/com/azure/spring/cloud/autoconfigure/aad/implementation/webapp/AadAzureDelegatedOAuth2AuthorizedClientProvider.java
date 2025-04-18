// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
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

import static com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.AZURE_DELEGATED;

/**
 * A strategy for authorizing (or re-authorizing) an OAuth 2.0 Client. This implementation implement {@link
 * Constants#AZURE_DELEGATED "azure_delegated" authorization grant type}.
 *
 * @see OAuth2AuthorizedClient
 * @see OAuth2AuthorizationContext
 * @see AuthorizationGrantType
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3">Section 1.3 Authorization Grant</a>
 * @since 3.8.0
 */
public class AadAzureDelegatedOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private final Clock clock;
    private final Duration clockSkew;
    private final OAuth2AuthorizedClientProvider provider;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    /**
     * Creates a new instance of {@link AadAzureDelegatedOAuth2AuthorizedClientProvider}.
     *
     * @param provider the OAuth2 token refresh provider
     * @param authorizedClientRepository the OAuth2 repository
     */
    public AadAzureDelegatedOAuth2AuthorizedClientProvider(
        RefreshTokenOAuth2AuthorizedClientProvider provider,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.clock = Clock.systemUTC();
        this.clockSkew = Duration.ofSeconds(60);
        this.provider = provider;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    /**
     * Attempt to authorize (or re-authorize) the
     * {@link OAuth2AuthorizationContext#getClientRegistration() client} in the provided
     * context. Implementations must return {@code null} if authorization is not supported
     * for the specified client, e.g. the provider doesn't support the
     * {@link ClientRegistration#getAuthorizationGrantType() authorization grant} type
     * configured for the client.
     * @param context the context that holds authorization-specific state for the client
     * @return the {@link OAuth2AuthorizedClient} or {@code null} if authorization is not
     * supported for the specified client
     */
    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");
        ClientRegistration clientRegistration = context.getClientRegistration();
        if (!AZURE_DELEGATED.equals(clientRegistration.getAuthorizationGrantType())) {
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
                       .orElseGet(AadAzureDelegatedOAuth2AuthorizedClientProvider::getDefaultHttpServletRequest);
    }

    private static HttpServletRequest getDefaultHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                       .filter(attributes -> attributes instanceof ServletRequestAttributes)
                       .map(attributes -> ((ServletRequestAttributes) attributes).getRequest())
                       .orElse(null);
    }
}
