// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Manage all AAD OAuth2 clients configured by property "spring.cloud.azure.active-directory.xxx".
 * Do extra works:
 * 1. Make "azure" client's scope contains all "azure_delegated" clients' scope.
 *    This scope is used to request authorize code.
 * 2. Save azureClientAccessTokenScopes, this scope is used to request "azure" client's access_token.
 */
public class AadReactiveClientRegistrationRepository implements ReactiveClientRegistrationRepository {

    private final AadClientRegistrationRepository repository;

    /**
     * Creates a new instance of {@link AadReactiveClientRegistrationRepository}.
     *
     * @param repository the underlying repository.
     */
    public AadReactiveClientRegistrationRepository(AadClientRegistrationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String registrationId) {
        return Mono.just(this.repository.findByRegistrationId(registrationId));
    }

    /**
     * Gets the set of Azure client access token scopes.
     *
     * @return the set of Azure client access token scopes
     */
    public Set<String> getAzureClientAccessTokenScopes() {
        return this.repository.getAzureClientAccessTokenScopes();
    }
}
