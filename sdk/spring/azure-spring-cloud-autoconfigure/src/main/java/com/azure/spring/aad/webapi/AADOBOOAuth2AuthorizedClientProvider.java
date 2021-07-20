// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADAuthorizationGrantType;
import com.azure.spring.autoconfigure.aad.Constants;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class AADOBOOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOBOOAuth2AuthorizedClientProvider.class);


    private final Clock clock = Clock.systemUTC();

    private final Duration clockSkew = Duration.ofSeconds(60);


    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        Assert.notNull(context, "context cannot be null");
        ClientRegistration clientRegistration = context.getClientRegistration();

        if (!AADAuthorizationGrantType.ON_BEHALF_OF
            .isSameGrantType(clientRegistration.getAuthorizationGrantType())) {
            return null;
        }

        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if (authorizedClient != null && !hasTokenExpired(authorizedClient.getAccessToken())) {
            // If client is already authorized but access token is NOT expired than no need for re-authorization
            return null;
        }

        return getOboAuthorizedClient(context.getClientRegistration(), context.getPrincipal());
    }

    private boolean hasTokenExpired(AbstractOAuth2Token token) {
        Instant expiresAt = token.getExpiresAt();
        if (expiresAt == null) {
            return true;
        }

        expiresAt = expiresAt.minus(this.clockSkew);
        return this.clock.instant().isAfter(expiresAt);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends OAuth2AuthorizedClient> T getOboAuthorizedClient(ClientRegistration clientRegistration,
                                                                        Authentication principal) {
        if (principal instanceof AnonymousAuthenticationToken) {
            LOGGER.debug("Found anonymous authentication.");
            return null;
        }

        if (!(principal instanceof AbstractOAuth2TokenAuthenticationToken)) {
            throw new IllegalStateException("Unsupported token implementation " + principal.getClass());
        }

        try {
            String accessToken = ((AbstractOAuth2TokenAuthenticationToken<?>) principal).getToken()
                .getTokenValue();
            OnBehalfOfParameters parameters = OnBehalfOfParameters
                .builder(clientRegistration.getScopes(), new UserAssertion(accessToken))
                .build();
            ConfidentialClientApplication clientApplication = createApp(clientRegistration);
            if (null == clientApplication) {
                return null;
            }

            String oboAccessToken = clientApplication.acquireToken(parameters).get().accessToken();
            JWT parser = JWTParser.parse(oboAccessToken);
            Date iat = (Date) parser.getJWTClaimsSet().getClaim("iat");
            Date exp = (Date) parser.getJWTClaimsSet().getClaim("exp");
            OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                oboAccessToken,
                Instant.ofEpochMilli(iat.getTime()),
                Instant.ofEpochMilli(exp.getTime()));
            OAuth2AuthorizedClient oAuth2AuthorizedClient = new OAuth2AuthorizedClient(clientRegistration,
                principal.getName(),
                oAuth2AccessToken);
            LOGGER.info("load obo authorized client success");
            return (T) oAuth2AuthorizedClient;
        } catch (ExecutionException exception) {
            // Handle conditional access policy, step 1.
            // A user interaction is required, but we are in a web API, and therefore, we need to report back to the
            // client through a 'WWW-Authenticate' header https://tools.ietf.org/html/rfc6750#section-3.1
            Optional.of(exception)
                .map(Throwable::getCause)
                .filter(e -> e instanceof MsalInteractionRequiredException)
                .map(e -> (MsalInteractionRequiredException) e)
                .map(MsalServiceException::claims)
                .filter(StringUtils::hasText)
                .ifPresent(this::replyForbiddenWithWwwAuthenticateHeader);
            LOGGER.error("Failed to load authorized client.", exception);
        } catch (InterruptedException | ParseException exception) {
            LOGGER.error("Failed to load authorized client.", exception);
        }
        return null;
    }

    ConfidentialClientApplication createApp(ClientRegistration clientRegistration) {
        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();
        String authority = interceptAuthorizationUri(authorizationUri);
        IClientSecret clientCredential = ClientCredentialFactory
            .createFromSecret(clientRegistration.getClientSecret());
        try {
            return ConfidentialClientApplication.builder(clientRegistration.getClientId(), clientCredential)
                .authority(authority)
                .build();
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to create ConfidentialClientApplication", e);
        }
        return null;
    }

    private String interceptAuthorizationUri(String authorizationUri) {
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
        return null;
    }

    private void replyForbiddenWithWwwAuthenticateHeader(String claims) {
        ServletRequestAttributes attr =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attr.getResponse();
        Assert.notNull(response, "HttpServletResponse should not be null.");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS, claims);
        parameters.put(OAuth2ParameterNames.ERROR, OAuth2ErrorCodes.INVALID_TOKEN);
        parameters.put(OAuth2ParameterNames.ERROR_DESCRIPTION, "The resource server requires higher privileges "
            + "than "
            + "provided by the access token");
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, Constants.BEARER_PREFIX + parameters.toString());
    }
}
