// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * To add conditional policy claims to authorization URL.
 *
 * @see OAuth2AuthorizationRequestResolver
 */
public class AadOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    private final AadAuthenticationProperties properties;

    /**
     * Creates a new instance of {@link AadOAuth2AuthorizationRequestResolver}.
     *
     * @param clientRegistrationRepository the client registration repository
     * @param properties the AAD authentication properties
     */
    public AadOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
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
    public AadOAuth2AuthorizationRequestResolver(
            final String authorizationRequestBaseUri,
            ClientRegistrationRepository clientRegistrationRepository,
            AadAuthenticationProperties properties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
        this.properties = properties;
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param request the {@code HttpServletRequest}
     * @return the resolved {@link OAuth2AuthorizationRequest} or {@code null} if not
     * available
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return addClaims(request, defaultResolver.resolve(request));
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param request the {@code HttpServletRequest}
     * @param clientRegistrationId the clientRegistrationId to use
     * @return the resolved {@link OAuth2AuthorizationRequest} or {@code null} if not
     * available
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return addClaims(request, defaultResolver.resolve(request, clientRegistrationId));
    }

    // Add claims to authorization-url
    private OAuth2AuthorizationRequest addClaims(HttpServletRequest httpServletRequest,
                                                 OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
        if (oAuth2AuthorizationRequest == null || httpServletRequest == null) {
            return oAuth2AuthorizationRequest;
        }
        // Handle conditional access policy, step 3.
        HttpSession httpSession = httpServletRequest.getSession(false);
        final Map<String, Object> additionalParameters = new HashMap<>();

        if (httpSession != null) {
            String conditionalAccessPolicyClaims = (String) httpSession.getAttribute(
                Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
            if (conditionalAccessPolicyClaims != null) {
                httpSession.removeAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
                additionalParameters.put(Constants.CLAIMS, conditionalAccessPolicyClaims);
            }
        }
        if (properties != null && properties.getAuthenticateAdditionalParameters() != null) {
            additionalParameters.putAll(properties.getAuthenticateAdditionalParameters());
        }
        additionalParameters.putAll(oAuth2AuthorizationRequest.getAdditionalParameters());
        return OAuth2AuthorizationRequest.from(oAuth2AuthorizationRequest)
            .additionalParameters(additionalParameters)
            .build();
    }
}
