// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

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

    public AzureClientRegistration(ClientRegistration client, Set<String> scopes) {
        this.client = client;
        this.accessTokenScopes = scopes;
    }

    public ClientRegistration getClient() {
        return client;
    }

    public Set<String> getAccessTokenScopes() {
        return accessTokenScopes;
    }
}
