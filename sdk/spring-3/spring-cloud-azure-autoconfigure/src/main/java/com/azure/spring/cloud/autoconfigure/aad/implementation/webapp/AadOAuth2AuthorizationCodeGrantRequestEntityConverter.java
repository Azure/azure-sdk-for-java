// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;
import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * Used to set "scope" parameter when use "auth-code" to get "access_token".
 *
 * @see AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter
 */
public class AadOAuth2AuthorizationCodeGrantRequestEntityConverter
    extends AbstractOAuth2AuthorizationCodeGrantRequestEntityConverter {

    private final Set<String> azureClientAccessTokenScopes;

    /**
     * Creates a new instance of {@link AadOAuth2AuthorizationCodeGrantRequestEntityConverter}.
     *
     * @param azureClientAccessTokenScopes the Azure client access token scopes
     */
    public AadOAuth2AuthorizationCodeGrantRequestEntityConverter(Set<String> azureClientAccessTokenScopes) {
        this.azureClientAccessTokenScopes = azureClientAccessTokenScopes;
    }


    /**
     * Get application id.
     *
     * @return application id
     */
    @Override
    protected String getApplicationId() {
        return AzureSpringIdentifier.AZURE_SPRING_AAD;
    }

    /**
     * Get http body.
     *
     * @return http body
     */
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
