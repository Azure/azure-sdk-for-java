// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapi;

import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identify platform.
 *
 * @since 4.3.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
public class AadJwtBearerGrantRequestEntityConverter extends JwtBearerGrantRequestEntityConverter {

    @Override
    protected MultiValueMap<String, String> createParameters(JwtBearerGrantRequest jwtBearerGrantRequest) {
        MultiValueMap<String, String> parameters = super.createParameters(jwtBearerGrantRequest);
        parameters.add("requested_token_use", "on_behalf_of");
        return parameters;
    }
}

