// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.Collections;
import java.util.UUID;

/**
 * When using "auth-code" in AAD and AAD B2C, it's used to expand head and body parameters of the request.
 */
public abstract class AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter
        extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private static final MultiValueMap<String, String> EMPTY_MULTI_VALUE_MAP =
            new MultiValueMapAdapter<>(Collections.emptyMap());

    protected AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter() {
        addHeadersConverter(this::getHttpHeaders);
        addParametersConverter(this::getHttpBody);
    }

    /**
     * Gets the application ID.
     *
     * @return the application ID
     */
    protected abstract String getApplicationId();

    /**
     * Additional default headers information.
     * @return HttpHeaders
     */
    protected HttpHeaders getHttpHeaders(OAuth2AuthorizationCodeGrantRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(getApplicationId()));
        httpHeaders.put("x-client-VER", Collections.singletonList(AzureSpringIdentifier.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }

    /**
     * Default body of OAuth2AuthorizationCodeGrantRequest.
     * @param request OAuth2AuthorizationCodeGrantRequest
     * @return MultiValueMap
     */
    protected MultiValueMap<String, String> getHttpBody(OAuth2AuthorizationCodeGrantRequest request) {
        return EMPTY_MULTI_VALUE_MAP;
    }
}
