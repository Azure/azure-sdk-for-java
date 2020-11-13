// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public class AzureOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final DefaultClient defaultClient;

    public AzureOAuth2AuthorizationCodeGrantRequestEntityConverter(DefaultClient defaultClient) {
        this.defaultClient = defaultClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> result = super.convert(request);
        if (isRequestForDefaultClient(request)) {
            Optional.ofNullable(result)
                    .map(HttpEntity::getBody)
                    .map(b -> (MultiValueMap<String, String>) b)
                    .ifPresent(map -> map.add("scope", scopeValue()));
        }
        return result;
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(defaultClient.getClientRegistration());
    }

    private String scopeValue() {
        return String.join(" ", defaultClient.getScope());
    }
}
