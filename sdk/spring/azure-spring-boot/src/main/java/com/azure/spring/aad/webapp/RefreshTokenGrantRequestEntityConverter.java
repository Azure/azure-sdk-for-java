// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * Used to set "scope" parameter when use "refresh_token" to get "access_token".
 */
public class RefreshTokenGrantRequestEntityConverter extends OAuth2RefreshTokenGrantRequestEntityConverter {

    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        RequestEntity<?> result = super.convert(refreshTokenGrantRequest);
        Optional.ofNullable(result)
            .map(HttpEntity::getBody)
            .map(b -> (MultiValueMap<String, String>) b)
            .ifPresent(body -> body.add("scope", scopeValue(refreshTokenGrantRequest)));
        return result;
    }

    private String scopeValue(OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest) {
        return String.join(" ", refreshTokenGrantRequest.getClientRegistration().getScopes());
    }
}
