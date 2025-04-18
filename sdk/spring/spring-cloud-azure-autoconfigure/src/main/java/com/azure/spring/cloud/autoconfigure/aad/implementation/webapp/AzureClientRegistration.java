// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Set;

/**
 * Azure oauth2 client registration.
 * It has 2 kind of scopes:
 * 1. AzureClientRegistration.client.scopes: used to authorize.
 * 2. AzureClientRegistration.accessTokenScopes: used to get access_token.
 */
public class AzureClientRegistration {

    private final ClientRegistration client;
    private final Set<String> accessTokenScopes;

    /**
     * Creates a new instance of {@link AzureClientRegistration}.
     *
     * @param client the client registration
     * @param scopes the set of access token scopes
     */
    public AzureClientRegistration(ClientRegistration client, Set<String> scopes) {
        this.client = client;
        this.accessTokenScopes = scopes;
    }

    /**
     * Gets the client registration.
     *
     * @return the client registration
     */
    public ClientRegistration getClient() {
        return client;
    }

    /**
     * Gets the set of access token scopes.
     *
     * @return the set of access token scopes
     */
    public Set<String> getAccessTokenScopes() {
        return accessTokenScopes;
    }
}
