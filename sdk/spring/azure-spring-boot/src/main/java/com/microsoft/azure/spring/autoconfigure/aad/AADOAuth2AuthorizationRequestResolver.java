/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

public class AADOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private OAuth2AuthorizationRequestResolver defaultResolver;

    public AADOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return addClaims(request, defaultResolver.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return addClaims(request, defaultResolver.resolve(request, clientRegistrationId));
    }

    //add claims to authorization-url
    private OAuth2AuthorizationRequest addClaims(HttpServletRequest request,
                                                 OAuth2AuthorizationRequest req) {
        if (req == null || request == null) {
            return req;
        }

        final String conditionalAccessPolicyClaims = getConditionalAccessPolicyClaims(request);
        if (StringUtils.isEmpty(conditionalAccessPolicyClaims)) {
            return req;
        }

        final Map<String, Object> extraParams = new HashMap();
        if (req.getAdditionalParameters() != null) {
            extraParams.putAll(req.getAdditionalParameters());
        }
        extraParams.put(AADConstantsHelper.CLAIMS, conditionalAccessPolicyClaims);
        return OAuth2AuthorizationRequest
                .from(req)
                .additionalParameters(extraParams)
                .build();
    }

    private String getConditionalAccessPolicyClaims(HttpServletRequest request) {
        //claims just for one use
        final String claims = request.getSession()
                .getAttribute(AADConstantsHelper.CAP_CLAIMS) == null ? "" : (String) request
                .getSession()
                .getAttribute(AADConstantsHelper.CAP_CLAIMS);
        //remove claims in session
        if (!StringUtils.isEmpty(claims)) {
            request.getSession().removeAttribute(AADConstantsHelper.CAP_CLAIMS);
        }
        return claims;
    }
}
