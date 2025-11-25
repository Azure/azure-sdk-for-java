// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identify platform.
 *
 * @since 4.3.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
public class AadJwtBearerGrantRequestParametersConverter
    implements Converter<JwtBearerGrantRequest, MultiValueMap<String,String>> {

    @Override
    public MultiValueMap<String, String> convert(JwtBearerGrantRequest jwtBearerGrantRequest) {
        ClientRegistration clientRegistration = jwtBearerGrantRequest.getClientRegistration();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add(OAuth2ParameterNames.GRANT_TYPE, jwtBearerGrantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.ASSERTION, jwtBearerGrantRequest.getJwt().getTokenValue());
        if (!CollectionUtils.isEmpty(clientRegistration.getScopes())) {
            parameters.add(OAuth2ParameterNames.SCOPE,
                StringUtils.collectionToDelimitedString(clientRegistration.getScopes(), " "));
        }
        if (ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(clientRegistration.getClientAuthenticationMethod())) {
            parameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
            parameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }
        parameters.add("requested_token_use", "on_behalf_of");
        return parameters;
    }
}
