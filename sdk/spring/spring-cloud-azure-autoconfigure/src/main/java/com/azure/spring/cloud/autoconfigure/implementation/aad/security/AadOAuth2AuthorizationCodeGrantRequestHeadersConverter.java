// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;

import java.util.Collections;
import java.util.UUID;

/**
 * When using "auth-code" in AAD, it's used to expand head parameters of the request.
 */
public class AadOAuth2AuthorizationCodeGrantRequestHeadersConverter
    implements Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> {

    @Override
    public HttpHeaders convert(OAuth2AuthorizationCodeGrantRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(AzureSpringIdentifier.AZURE_SPRING_AAD));
        httpHeaders.put("x-client-VER", Collections.singletonList(AzureSpringIdentifier.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }
}
