// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * To add conditional policy claims to authorization URL.
 *
 * @see OAuth2AuthorizationRequestResolver
 */
public class AadServerOAuth2AuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {
    private final ServerOAuth2AuthorizationRequestResolver defaultResolver;

    private final AadAuthenticationProperties properties;

    /**
     * Creates a new instance of {@link AadOAuth2AuthorizationRequestResolver}.
     *
     * @param clientRegistrationRepository the client registration repository
     * @param properties the AAD authentication properties
     */
    public AadServerOAuth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                 AadAuthenticationProperties properties) {
        this(
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI,
            clientRegistrationRepository,
            properties);
    }

    /**
     * Creates a new instance of {@link AadOAuth2AuthorizationRequestResolver}.
     *
     * @param authorizationRequestBaseUri the client registration repository
     * @param clientRegistrationRepository the client registration repository
     * @param properties the AAD authentication properties
     */
    public AadServerOAuth2AuthorizationRequestResolver(
        final String authorizationRequestBaseUri,
        ReactiveClientRegistrationRepository clientRegistrationRepository,
        AadAuthenticationProperties properties) {
        this.defaultResolver = new DefaultServerOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, ServerWebExchangeMatchers.pathMatchers(authorizationRequestBaseUri));
        this.properties = properties;
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param exchange the {@code ServerWebExchange}
     * @return the resolved {@link OAuth2AuthorizationRequest}
     * available
     */
    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return addClaims(exchange, defaultResolver.resolve(exchange));
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param exchange the {@code ServerWebExchange}
     * @param clientRegistrationId the clientRegistrationId to use
     * @return the resolved {@link OAuth2AuthorizationRequest}
     * available
     */
    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        return addClaims(exchange, defaultResolver.resolve(exchange, clientRegistrationId));
    }

    // Add claims to authorization-url
    private Mono<OAuth2AuthorizationRequest> addClaims(ServerWebExchange exchange,
                                                       Mono<OAuth2AuthorizationRequest> oAuth2AuthorizationRequest) {
        if (oAuth2AuthorizationRequest == null || exchange == null) {
            return oAuth2AuthorizationRequest;
        }

        return oAuth2AuthorizationRequest.flatMap(request -> exchange.getSession().map(session -> {
            final Map<String, Object> additionalParameters = new HashMap<>();

            String conditionalAccessPolicyClaims = (String) session.getAttribute(
                Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
            if (conditionalAccessPolicyClaims != null) {
                session.getAttributes().remove(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
                additionalParameters.put(Constants.CLAIMS, conditionalAccessPolicyClaims);
            }
            if (properties != null && properties.getAuthenticateAdditionalParameters() != null) {
                additionalParameters.putAll(properties.getAuthenticateAdditionalParameters());
            }
            additionalParameters.putAll(request.getAdditionalParameters());
            return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(additionalParameters)
                .build();
        }));
    }
}
