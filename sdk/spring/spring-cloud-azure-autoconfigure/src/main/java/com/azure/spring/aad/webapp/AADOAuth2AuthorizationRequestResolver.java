// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.Constants;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * To add conditional policy claims to authorization URL.
 */
public class AADOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    private final AADAuthenticationProperties properties;

    public AADOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                                 AADAuthenticationProperties properties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
        this.properties = properties;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return addClaims(request, defaultResolver.resolve(request));
    }

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
        final String conditionalAccessPolicyClaims =
            Optional.of(httpServletRequest)
                    .map(HttpServletRequest::getSession)
                    .map(httpSession -> {
                        String claims = (String) httpSession.getAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
                        if (claims != null) {
                            httpSession.removeAttribute(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
                        }
                        return claims;
                    })
                    .orElse(null);
        final Map<String, Object> additionalParameters = new HashMap<>();
        if (conditionalAccessPolicyClaims != null) {
            additionalParameters.put(Constants.CLAIMS, conditionalAccessPolicyClaims);
        }
        Optional.ofNullable(properties)
                .map(AADAuthenticationProperties::getAuthenticateAdditionalParameters)
                .ifPresent(additionalParameters::putAll);
        Optional.of(oAuth2AuthorizationRequest)
                .map(OAuth2AuthorizationRequest::getAdditionalParameters)
                .ifPresent(additionalParameters::putAll);
        return OAuth2AuthorizationRequest.from(oAuth2AuthorizationRequest)
                                         .additionalParameters(additionalParameters)
                                         .build();
    }
}
