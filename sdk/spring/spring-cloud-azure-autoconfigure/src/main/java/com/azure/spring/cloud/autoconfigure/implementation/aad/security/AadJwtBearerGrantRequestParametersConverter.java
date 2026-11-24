// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identify platform.
 * This converter only adds the Azure-specific parameter "requested_token_use" with value "on_behalf_of".
 * The standard OAuth2 parameters (grant_type, assertion, client_id, etc.) are added by Spring Security's
 * DefaultOAuth2TokenRequestParametersConverter, which is automatically included when using
 * RestClientJwtBearerTokenResponseClient.
 *
 * @since 7.0.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
public class AadJwtBearerGrantRequestParametersConverter
    implements Converter<JwtBearerGrantRequest, MultiValueMap<String, String>> {

    @Override
    public MultiValueMap<String, String> convert(JwtBearerGrantRequest jwtBearerGrantRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("requested_token_use", "on_behalf_of");
        return parameters;
    }
}
