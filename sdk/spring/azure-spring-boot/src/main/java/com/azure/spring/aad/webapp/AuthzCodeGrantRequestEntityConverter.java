// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 */
public class AuthzCodeGrantRequestEntityConverter extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final AzureClientRegistration azureClient;

    public AuthzCodeGrantRequestEntityConverter(AzureClientRegistration client) {
        azureClient = client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> result = super.convert(request);
        String scopes = String.join(" ", isRequestForDefaultClient(request) ?
            azureClient.getAccessTokenScopes() :
            request.getClientRegistration().getScopes());
        Optional.ofNullable(result)
                .map(HttpEntity::getBody)
                .map(b -> (MultiValueMap<String, String>) b)
                .ifPresent(body -> body.add("scope", scopes));
        return result;
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(azureClient.getClient());
    }
}
