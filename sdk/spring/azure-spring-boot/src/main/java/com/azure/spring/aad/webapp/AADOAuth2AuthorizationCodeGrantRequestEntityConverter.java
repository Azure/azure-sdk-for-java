// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.utils.ApplicationId;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 */
public class AADOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final AzureClientRegistration azureClient;

    public AADOAuth2AuthorizationCodeGrantRequestEntityConverter(AzureClientRegistration client) {
        this.azureModule = ApplicationId.AZURE_SPRING_AAD;
        azureClient = client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        return super.convert(request);
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(azureClient.getClient());
    }

    @Override
    public MultiValueMap<String, String> getHttpBody(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String scopes = String.join(" ", isRequestForDefaultClient(request)
            ? azureClient.getAccessTokenScopes()
            : request.getClientRegistration().getScopes());
        body.add("scope", scopes);
        return body;
    }
}
