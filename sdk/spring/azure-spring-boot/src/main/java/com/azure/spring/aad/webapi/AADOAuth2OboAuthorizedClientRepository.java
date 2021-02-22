// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.autoconfigure.aad.Constants;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * OAuth2AuthorizedClientRepository for obo flow
 * </p>
 */
public class AADOAuth2OboAuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADOAuth2OboAuthorizedClientRepository.class);

    private static final String OBO_AUTHORIZEDCLIENT_PREFIX = "obo_authorizedclient_";

    private final ClientRegistrationRepository repository;

    public AADOAuth2OboAuthorizedClientRepository(ClientRegistrationRepository repository) {
        this.repository = repository;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
        String oboAuthorizedClientAttributeName = OBO_AUTHORIZEDCLIENT_PREFIX + registrationId;
        if (request.getAttribute(oboAuthorizedClientAttributeName) != null) {
            return (T) request.getAttribute(oboAuthorizedClientAttributeName);
        }

        if (!(authentication instanceof AbstractOAuth2TokenAuthenticationToken)) {
            throw new IllegalStateException("Unsupported token implementation " + authentication.getClass());
        }

        try {
            String accessToken = ((AbstractOAuth2TokenAuthenticationToken<?>) authentication).getToken()
                                                                                             .getTokenValue();
            ClientRegistration clientRegistration = repository.findByRegistrationId(registrationId);
            if (clientRegistration == null) {
                LOGGER.warn("Not found the ClientRegistration, registrationId={}", registrationId);
                return null;
            }

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
                authentication.getName(),
                oAuth2AccessToken);
            request.setAttribute(oboAuthorizedClientAttributeName, (T) oAuth2AuthorizedClient);
            return (T) oAuth2AuthorizedClient;
        } catch (ExecutionException exception) {
            // Handle conditional access policy for obo flow.
            // A user interaction is required, but we are in a web API, and therefore, we need to report back to the
            // client through a 'WWW-Authenticate' header https://tools.ietf.org/html/rfc6750#section-3.1
            Optional.of(exception)
                    .map(Throwable::getCause)
                    .filter(e -> e instanceof MsalInteractionRequiredException)
                    .map(e -> (MsalInteractionRequiredException) e)
                    .ifPresent(this::replyForbiddenWithWwwAuthenticateHeader);
            LOGGER.error("Failed to load authorized client.", exception);
        } catch (InterruptedException | ParseException exception) {
            LOGGER.error("Failed to load authorized client.", exception);
        }
        return null;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient, Authentication authentication,
                                     HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication authentication,
                                       HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
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

    void replyForbiddenWithWwwAuthenticateHeader(MsalInteractionRequiredException exception) {
        ServletRequestAttributes attr =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attr.getResponse();
        Assert.notNull(response, "HttpServletResponse should not be null.");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS, exception.claims());
        parameters.put(OAuth2ParameterNames.ERROR, OAuth2ErrorCodes.INVALID_TOKEN);
        parameters.put(OAuth2ParameterNames.ERROR_DESCRIPTION, "The resource server requires higher privileges than "
            + "provided by the access token");
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, Constants.BEARER_PREFIX + parameters.toString());
    }
}
