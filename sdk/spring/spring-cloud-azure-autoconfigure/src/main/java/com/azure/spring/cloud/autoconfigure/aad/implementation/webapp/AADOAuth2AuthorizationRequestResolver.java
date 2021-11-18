// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants;
import com.azure.spring.cloud.autoconfigure.aad.implementation.properties.AADAuthenticationProperties;
import javax.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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
        if (properties != null) {
            additionalParameters.putAll(properties.getAuthenticateAdditionalParameters());
        }
        additionalParameters.putAll(oAuth2AuthorizationRequest.getAdditionalParameters());
        return OAuth2AuthorizationRequest.from(oAuth2AuthorizationRequest)
            .additionalParameters(additionalParameters)
            .build();
    }
}
