// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * This converter is to add 'scope' parameter when request for access token.
 *
 * In default oidc flow, when request for access token by authorization code, 'scope' parameter is not necessary.
 * Because one consent operation only create one authorizedClient.
 *
 * But for Microsoft Authorization Server, one consent can created multiple authorizedClient.
 * So scope parameter is necessary when request for access token.
 *
 * Refs:
 * 1. https://tools.ietf.org/html/rfc6749#section-4.1.3
 * 2. https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-access-token
 */
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
