// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.utils.ApplicationId;
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
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 */
public class AADOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final AzureClientRegistration azureClient;

    public AADOAuth2AuthorizationCodeGrantRequestEntityConverter(AzureClientRegistration client) {
        azureClient = client;
    }

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
        String scopes = String.join(" ", isRequestForDefaultClient(request)
            ? azureClient.getAccessTokenScopes()
            : request.getClientRegistration().getScopes());
        body.add("scope", scopes);

        return new RequestEntity<>(body, httpHeaders, requestEntity.getMethod(), requestEntity.getUrl());
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(azureClient.getClient());
    }

    static HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(ApplicationId.AZURE_SPRING_AAD));
        httpHeaders.put("x-client-VER", Collections.singletonList(ApplicationId.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }
}
