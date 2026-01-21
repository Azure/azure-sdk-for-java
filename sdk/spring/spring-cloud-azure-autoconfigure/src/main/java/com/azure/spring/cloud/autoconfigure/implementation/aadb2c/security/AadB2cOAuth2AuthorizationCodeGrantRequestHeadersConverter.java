// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

import java.util.Collections;
import java.util.UUID;

/**
 * Converter that adds Azure AD B2C specific HTTP headers to authorization code grant requests.
 * These headers are used for telemetry and tracking purposes by Azure AD B2C.
 */
public class AadB2cOAuth2AuthorizationCodeGrantRequestHeadersConverter
    implements Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> {

    @Override
    public HttpHeaders convert(OAuth2AuthorizationCodeGrantRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.put("x-client-SKU", Collections.singletonList(AzureSpringIdentifier.AZURE_SPRING_B2C));
        headers.put("x-client-VER", Collections.singletonList(AzureSpringIdentifier.VERSION));
        headers.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return headers;
    }
}
