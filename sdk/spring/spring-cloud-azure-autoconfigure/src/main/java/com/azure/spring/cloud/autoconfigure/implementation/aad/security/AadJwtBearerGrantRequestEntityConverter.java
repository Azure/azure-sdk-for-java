// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identity platform.
 *
 * @since 4.3.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
@SuppressWarnings({"deprecation", "removal"})
public class AadJwtBearerGrantRequestEntityConverter extends JwtBearerGrantRequestEntityConverter {

    @Override
    protected MultiValueMap<String, String> createParameters(JwtBearerGrantRequest jwtBearerGrantRequest) {
        ClientRegistration clientRegistration = jwtBearerGrantRequest.getClientRegistration();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set(OAuth2ParameterNames.GRANT_TYPE, jwtBearerGrantRequest.getGrantType().getValue());
        parameters.set(OAuth2ParameterNames.ASSERTION, jwtBearerGrantRequest.getJwt().getTokenValue());
        if (!CollectionUtils.isEmpty(clientRegistration.getScopes())) {
            parameters.set(OAuth2ParameterNames.SCOPE,
                    StringUtils.collectionToDelimitedString(clientRegistration.getScopes(), " "));
        }
        // For CLIENT_SECRET_BASIC: credentials go in Authorization header, not in request parameters
        // For CLIENT_SECRET_POST and other methods: client_id goes in request parameters
        if (!ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            parameters.set(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
        }
        // For CLIENT_SECRET_POST: client_secret goes in request parameters
        // For CLIENT_SECRET_BASIC and other methods: client_secret is handled separately (e.g., in Authorization header)
        if (ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(clientRegistration.getClientAuthenticationMethod())) {
            parameters.set(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }
        parameters.set("requested_token_use", "on_behalf_of");
        return parameters;
    }
}

