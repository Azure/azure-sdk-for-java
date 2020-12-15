// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

/**
 * Used to set "scope" parameter when use "refresh_token" to get "access_token".
 */
public class RefreshTokenGrantRequestEntityConverter extends OAuth2RefreshTokenGrantRequestEntityConverter {

    private final List<ClientRegistration> otherClients;

    public RefreshTokenGrantRequestEntityConverter(List<ClientRegistration> otherClients) {
        this.otherClients = otherClients;
    }

    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        RequestEntity<?> result = super.convert(refreshTokenGrantRequest);
        if (isRequestForDefaultClient(refreshTokenGrantRequest)) {
            Optional.ofNullable(result)
                .map(HttpEntity::getBody)
                .map(b -> (MultiValueMap<String, String>) b)
                .ifPresent(body -> body.add("scope", scopeValue(refreshTokenGrantRequest)));
        }
        return result;
    }

    private boolean isRequestForDefaultClient(OAuth2RefreshTokenGrantRequest request) {
        return otherClients.contains(request.getClientRegistration());
    }

    private String scopeValue(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        return String.join(" ", refreshTokenGrantRequest.getClientRegistration().getScopes());
    }
}
