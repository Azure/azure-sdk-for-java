// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.oauth2.client;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.azure.spring.security.oauth2.client.implementation.OnBehalfOfHttpClient.getOnBehalfOfAccessToken;

/**
 * A strategy for authorizing (or re-authorizing) an OAuth 2.0 Client. This implementation is used in resource server.
 * Use current access token to request another access token.
 *
 * <p>
 * Example:
 * <pre><code>
 * {@literal @}Bean
 * public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
 *                                                              OAuth2AuthorizedClientRepository authorizedClients) {
 *     DefaultOAuth2AuthorizedClientManager manager =
 *         new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
 *     OnBehalfOfOAuth2AuthorizedClientProvider oboProvider = new OnBehalfOfOAuth2AuthorizedClientProvider();
 *     OAuth2AuthorizedClientProvider authorizedClientProviders =
 *         OAuth2AuthorizedClientProviderBuilder.builder()
 *                                              .provider(oboProvider)
 *                                              .build();
 *     manager.setAuthorizedClientProvider(authorizedClientProviders);
 *     return manager;
 * }
 * </code></pre>
 *
 * @author RujunChen
 * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">On-Behalf-Of flow</a>
 * @since 4.0
 */
public class OnBehalfOfOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnBehalfOfOAuth2AuthorizedClientProvider.class);

    private Clock clock = Clock.systemUTC();
    private Duration clockSkew = Duration.ofSeconds(60);

    /**
     * Sets the maximum acceptable clock skew, which is used when checking the {@link
     * OAuth2AuthorizedClient#getAccessToken() access token} expiry. The default is 60 seconds.
     *
     * <p>
     * An access token is considered expired if {@code OAuth2AccessToken#getExpiresAt() - clockSkew} is before the
     * current time {@code clock#instant()}.
     *
     * @param clockSkew the maximum acceptable clock skew
     */
    public void setClockSkew(Duration clockSkew) {
        Assert.notNull(clockSkew, "clockSkew cannot be null");
        Assert.isTrue(clockSkew.getSeconds() >= 0, "clockSkew must be >= 0");
        this.clockSkew = clockSkew;
    }

    /**
     * Sets the {@link Clock} used in {@link Instant#now(Clock)} when checking the access token expiry.
     *
     * @param clock the clock
     */
    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");
        ClientRegistration clientRegistration = context.getClientRegistration();

        if (!clientRegistration.getAuthorizationGrantType().getValue().equals("on_behalf_of")) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if (authorizedClient != null && !hasTokenExpired(authorizedClient.getAccessToken())) {
            // If client is already authorized but access token is NOT expired than no need for re-authorization
            return null;
        }
        Authentication principal = context.getPrincipal();
        if (!(principal instanceof AbstractOAuth2TokenAuthenticationToken)) {
            return null;
        }
        try {
            return getOboAuthorizedClient(context.getClientRegistration(), principal);
        } catch (MalformedURLException | ExecutionException | ParseException |InterruptedException ex) {
            OAuth2Error oauth2Error = new OAuth2Error("get_on_bahalf_of_token_failed",
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
                    + ex.getMessage(),
                null);
            throw new ClientAuthorizationException(oauth2Error, clientRegistration.getRegistrationId(), ex);
        }
    }

    private boolean hasTokenExpired(OAuth2Token token) {
        return this.clock.instant().isAfter(token.getExpiresAt().minus(this.clockSkew));
    }

    @SuppressWarnings({ "unchecked" })
    private <T extends OAuth2AuthorizedClient> T getOboAuthorizedClient(
        ClientRegistration clientRegistration, Authentication principal)
        throws MalformedURLException, ExecutionException, InterruptedException, ParseException {
        String oboAccessToken = getOnBehalfOfAccessToken(
            toAuthority(clientRegistration.getProviderDetails().getAuthorizationUri()),
            clientRegistration.getClientId(),
            clientRegistration.getClientSecret(),
            ((AbstractOAuth2TokenAuthenticationToken<?>) principal).getToken().getTokenValue(),
            clientRegistration.getScopes());
        return (T) new OAuth2AuthorizedClient(clientRegistration,
            principal.getName(),
            toOAuth2AccessToken(oboAccessToken));
    }

    private OAuth2AccessToken toOAuth2AccessToken(String accessToken) throws ParseException {
        JWT parser = JWTParser.parse(accessToken);
        Date issuedAt = (Date) parser.getJWTClaimsSet().getClaim("iat");
        Date expiredAt = (Date) parser.getJWTClaimsSet().getClaim("exp");
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
            accessToken,
            Instant.ofEpochMilli(issuedAt.getTime()),
            Instant.ofEpochMilli(expiredAt.getTime()));
    }

    static String toAuthority(String authorizationUri) throws MalformedURLException {
        int count = 0;
        int slashNumber = 4;
        for (int i = 0; i < authorizationUri.length(); i++) {
            if (authorizationUri.charAt(i) == '/') {
                count++;
            }
            if (count == slashNumber) {
                return authorizationUri.substring(0, i + 1);
            }
        }
        throw new MalformedURLException("In valid authorizationUri. Valid authorizationUri example: "
            + "https://login.microsoftonline.com/tenant-id/oauth2/v2.0/authorize, "
            + "Refs: https://docs.microsoft.com/en-us/azure/active-directory/develop/authentication-national-cloud");
    }
}
