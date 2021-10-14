// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.core.ApplicationId;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * When using "auth-code" in AAD and AAD B2C, it's used to expand head and body parameters of the request.
 */
public abstract class AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    protected abstract String getApplicationId();

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> requestEntity = super.convert(request);
        Assert.notNull(requestEntity, "requestEntity can not be null");

        HttpHeaders httpHeaders = getHttpHeaders();
        Optional.of(requestEntity)
                .map(HttpEntity::getHeaders)
                .ifPresent(headers -> headers.forEach(httpHeaders::put));
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) requestEntity.getBody();
        Assert.notNull(body, "body can not be null");
        Optional.ofNullable(getHttpBody(request)).ifPresent(body::putAll);
        return new RequestEntity<>(body, httpHeaders, requestEntity.getMethod(), requestEntity.getUrl());
    }

    /**
     * Additional default headers information.
     * @return HttpHeaders
     */
    public HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(getApplicationId()));
        httpHeaders.put("x-client-VER", Collections.singletonList(ApplicationId.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }

    /**
     * Default body of OAuth2AuthorizationCodeGrantRequest.
     * @param request OAuth2AuthorizationCodeGrantRequest
     * @return MultiValueMap
     */
    public MultiValueMap<String, String> getHttpBody(OAuth2AuthorizationCodeGrantRequest request) {
        return null;
    }
}
