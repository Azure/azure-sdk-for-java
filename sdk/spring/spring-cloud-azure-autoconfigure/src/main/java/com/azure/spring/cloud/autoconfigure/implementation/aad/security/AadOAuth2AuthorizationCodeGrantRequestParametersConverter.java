// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;
import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 */
public class AadOAuth2AuthorizationCodeGrantRequestParametersConverter
    implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

    private final Set<String> azureClientAccessTokenScopes;

    /**
     * Creates a new instance of {@link AadOAuth2AuthorizationCodeGrantRequestParametersConverter}.
     *
     * @param azureClientAccessTokenScopes the Azure client access token scopes
     */
    public AadOAuth2AuthorizationCodeGrantRequestParametersConverter(Set<String> azureClientAccessTokenScopes) {
        this.azureClientAccessTokenScopes = azureClientAccessTokenScopes;
    }

    @Override
    public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        // Add custom scope for Azure client
        String scopes = String.join(" ", isRequestForAzureClient(request)
            ? azureClientAccessTokenScopes
            : request.getClientRegistration().getScopes());
        parameters.add("scope", scopes);

        return parameters;
    }

    private boolean isRequestForAzureClient(OAuth2AuthorizationCodeGrantRequest request) {
        return Optional.of(request)
                       .map(AbstractOAuth2AuthorizationGrantRequest::getClientRegistration)
                       .map(ClientRegistration::getRegistrationId)
                       .map(id -> id.equals(AZURE_CLIENT_REGISTRATION_ID))
                       .orElse(false);
    }
}
