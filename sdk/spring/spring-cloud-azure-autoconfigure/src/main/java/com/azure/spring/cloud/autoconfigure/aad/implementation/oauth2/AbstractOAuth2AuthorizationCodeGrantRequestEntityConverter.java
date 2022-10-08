// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.UUID;

/**
 * When using "auth-code" in AAD and AAD B2C, it's used to expand head and body parameters of the request.
 */
public abstract class AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    protected AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter() {
        addHeadersConverter((request) -> getHttpHeaders());
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
    public HttpHeaders getHttpHeaders() {
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
    public MultiValueMap<String, String> getHttpBody(OAuth2AuthorizationCodeGrantRequest request) {
        return new LinkedMultiValueMap<>();
    }
}
