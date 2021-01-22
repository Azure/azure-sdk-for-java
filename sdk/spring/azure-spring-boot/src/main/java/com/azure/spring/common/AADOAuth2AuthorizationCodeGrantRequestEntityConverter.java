// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.common;

import com.azure.spring.aad.webapp.AzureClientRegistration;
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
 * When using "auth-code" in AAD and AAD B2C, it's used to set the azure service header tag.
 * When using in AAD, it also sets  "scope" parameter for the request.
 */
public class AADOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final AzureClientRegistration azureClient;
    private final String xClientSku;

    public AADOAuth2AuthorizationCodeGrantRequestEntityConverter(String xClientSku, AzureClientRegistration client) {
        Assert.notNull(client, "azure client can not be null");
        this.xClientSku = xClientSku;
        this.azureClient = client;
    }

    public AADOAuth2AuthorizationCodeGrantRequestEntityConverter(String xClientSku) {
        this.xClientSku = xClientSku;
        this.azureClient = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> requestEntity = super.convert(request);
        Assert.notNull(requestEntity, "requestEntity can not be null");

        HttpHeaders httpHeaders = getHttpHeaders(xClientSku);
        Optional.of(requestEntity)
                .map(HttpEntity::getHeaders)
                .ifPresent(headers -> headers.forEach(httpHeaders::put));

        MultiValueMap<String, String> body = (MultiValueMap<String, String>) requestEntity.getBody();
        Assert.notNull(body, "body can not be null");
        if (ApplicationId.AZURE_SPRING_AAD.equalsIgnoreCase(xClientSku)) {
            String scopes = String.join(" ", isRequestForDefaultClient(request)
                ? azureClient.getAccessTokenScopes()
                : request.getClientRegistration().getScopes());
            body.add("scope", scopes);
        }
        return new RequestEntity<>(body, httpHeaders, requestEntity.getMethod(), requestEntity.getUrl());
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(azureClient.getClient());
    }

    public static HttpHeaders getHttpHeaders(String xClientSku) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("x-client-SKU", Collections.singletonList(xClientSku));
        httpHeaders.put("x-client-VER", Collections.singletonList(ApplicationId.VERSION));
        httpHeaders.put("client-request-id", Collections.singletonList(UUID.randomUUID().toString()));
        return httpHeaders;
    }
}
