// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.core.ApplicationId;
import com.azure.spring.aad.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;
import java.util.Set;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 */
public class AADOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final Set<String> azureClientAccessTokenScopes;

    public AADOAuth2AuthorizationCodeGrantRequestEntityConverter(Set<String> azureClientAccessTokenScopes) {
        this.azureClientAccessTokenScopes = azureClientAccessTokenScopes;
    }

    @Override
    protected String getApplicationId() {
        return ApplicationId.AZURE_SPRING_AAD;
    }

    @Override
    public MultiValueMap<String, String> getHttpBody(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String scopes = String.join(" ", isRequestForAzureClient(request)
            ? azureClientAccessTokenScopes
            : request.getClientRegistration().getScopes());
        body.add("scope", scopes);
        return body;
    }

    private boolean isRequestForAzureClient(OAuth2AuthorizationCodeGrantRequest request) {
        return Optional.of(request)
                       .map(AbstractOAuth2AuthorizationGrantRequest::getClientRegistration)
                       .map(ClientRegistration::getRegistrationId)
                       .map(id -> id.equals(AZURE_CLIENT_REGISTRATION_ID))
                       .orElse(false);
    }
}
