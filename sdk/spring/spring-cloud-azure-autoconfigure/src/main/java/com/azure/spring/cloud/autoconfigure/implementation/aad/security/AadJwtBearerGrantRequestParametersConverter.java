// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.util.MultiValueMap;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identify platform.
 *
 * @since 7.0.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
public class AadJwtBearerGrantRequestParametersConverter
    implements Converter<JwtBearerGrantRequest, MultiValueMap<String, String>> {

    private final DefaultOAuth2TokenRequestParametersConverter<JwtBearerGrantRequest> delegate =
        new DefaultOAuth2TokenRequestParametersConverter<>();

    @Override
    public MultiValueMap<String, String> convert(JwtBearerGrantRequest jwtBearerGrantRequest) {
        MultiValueMap<String, String> parameters = delegate.convert(jwtBearerGrantRequest);
        parameters.add("requested_token_use", "on_behalf_of");
        return parameters;
    }
}
