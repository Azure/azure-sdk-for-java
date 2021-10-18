// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.oauth2.client;

import com.azure.spring.security.oauth2.client.endpoint.AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * An implementation of an {@link OAuth2AuthorizedClientProvider} for the <strong>other_client_refresh_token</strong>
 * grant type. This implementation use other {@link OAuth2AuthorizedClient}'s refresh token to build a new {@link
 * OAuth2AuthorizedClient}.
 * <p>
 * <br/> <br/>
 * <strong>spring-security-oauth2-client</strong> only supports authorize {@link OAuth2AuthorizedClient} one by one.
 * But for <strong>Azure AD</strong>, multiple {@link ClientRegistration}s with same {@link
 * ClientRegistration.ProviderDetails} can be consented together.
 * <p>
 * <br/> <br/> For example: <br/> If you have 3 {@link ClientRegistration}s: client1, client2, client3. All the 3
 * clients have the same {@link ClientRegistration.ProviderDetails}. <br/> To consent the 3 clients together, you need
 * do these steps: <br/>
 * <ol>
 * <li>Set client1's {@link AuthorizationGrantType} to <strong>authorization_code</strong>.</li>
 * <li>Set client2 and client3's {@link AuthorizationGrantType} to <strong>client1_refresh_token</strong>.</li>
 * <li>Set client1's scopes, let client1's scopes include all client2 and client3's scopes. </li>
 * <li>Now client1's scopes covered multiple resources, this is allowed when request an authorization code.
 * But when request an access token, scopes must from a single resource. So we need to correct the scopes when
 * request an access token. We can use {@link AddScopeOAuth2AuthorizationCodeGrantRequestEntityConverter} to achieve
 * that.
 * </li>
 * </ol>
 * <p>
 * Sample code of {@link OtherClientRefreshTokenOAuth2AuthorizedClientProvider} usage:
 * <pre>
 * <code>
 * {@literal @}Bean
 *     public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
 *                                                                  OAuth2AuthorizedClientRepository authorizedClients) {
 *         DefaultOAuth2AuthorizedClientManager manager =
 *             new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
 *         OtherClientRefreshTokenOAuth2AuthorizedClientProvider provider =
 *             new OtherClientRefreshTokenOAuth2AuthorizedClientProvider("client1", authorizedClients);
 *         OAuth2AuthorizedClientProvider providers =
 *             OAuth2AuthorizedClientProviderBuilder.builder()
 *                                                  .authorizationCode()
 *                                                  .refreshToken()
 *                                                  .provider(provider)
 *                                                  .build();
 *         manager.setAuthorizedClientProvider(providers);
 *         return manager;
 *     }
 * </code>
 * </pre>
 *
 * @author RujunChen
 * @see OAuth2AuthorizedClient
 * @see ClientRegistration
 * @see ClientRegistration.ProviderDetails
 * @see AuthorizationGrantType
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-1.5">Section 1.5 Refresh Token</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1">Section 4.1 Authorization Code Grant</a>
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow">Azure AD auth code
 * grant</a>
 * @since 4.0
 */
public class OtherClientRefreshTokenOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private final Clock clock;
    private final Duration clockSkew;
    private final String otherClientId;
    private final String targetAuthorizationGrantType;
    private final OAuth2AuthorizedClientProvider provider;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public OtherClientRefreshTokenOAuth2AuthorizedClientProvider(
        String otherClientId,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this(otherClientId, authorizedClientRepository, new RefreshTokenOAuth2AuthorizedClientProvider());
    }

    public OtherClientRefreshTokenOAuth2AuthorizedClientProvider(
        String otherClientId,
        OAuth2AuthorizedClientRepository authorizedClientRepository,
        RefreshTokenOAuth2AuthorizedClientProvider provider) {
        this.clock = Clock.systemUTC();
        this.clockSkew = Duration.ofSeconds(60);
        this.otherClientId = otherClientId;
        this.targetAuthorizationGrantType = toTargetAuthorizationGrantType(otherClientId);
        this.provider = provider;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public static String toTargetAuthorizationGrantType(String otherClientId) {
        return otherClientId + "_refresh_token";
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");
        ClientRegistration clientRegistration = context.getClientRegistration();
        if (!clientRegistration.getAuthorizationGrantType().getValue().equals(targetAuthorizationGrantType)) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if (authorizedClient != null && tokenNotExpired(authorizedClient.getAccessToken())) {
            return null;
        }
        Authentication principal = context.getPrincipal();
        OAuth2AuthorizedClient otherClient = authorizedClientRepository.loadAuthorizedClient(
            otherClientId,
            principal,
            getHttpServletRequestOrDefault(context));
        if (otherClient == null) {
            throw new ClientAuthorizationRequiredException(otherClientId);
        }
        OAuth2AuthorizedClient clientWithExpiredToken =
            createClientWithExpiredToken(otherClient, clientRegistration, principal);
        String[] scopes = clientRegistration.getScopes().toArray(new String[0]);
        OAuth2AuthorizationContext refreshTokenAuthorizationContext =
            OAuth2AuthorizationContext.withAuthorizedClient(clientWithExpiredToken)
                                      .principal(principal)
                                      .attributes(attributes -> attributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME, scopes))
                                      .build();
        return provider.authorize(refreshTokenAuthorizationContext);
    }

    private boolean tokenNotExpired(OAuth2Token token) {
        return token.getExpiresAt() != null
            && this.clock.instant().isAfter(token.getExpiresAt().minus(this.clockSkew));
    }

    private OAuth2AuthorizedClient createClientWithExpiredToken(OAuth2AuthorizedClient otherClient,
                                                                ClientRegistration clientRegistration,
                                                                Authentication principal) {
        Assert.notNull(otherClient, "otherClient cannot be null");
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
            otherClient.getRefreshToken()
        );
    }

    private static HttpServletRequest getHttpServletRequestOrDefault(OAuth2AuthorizationContext context) {
        if (context.getAttributes().containsKey(HttpServletRequest.class.getName())) {
            return (HttpServletRequest) context.getAttributes().get(HttpServletRequest.class.getName());
        }
        return defaultHttpServletRequest();
    }

    private static HttpServletRequest defaultHttpServletRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }
}
