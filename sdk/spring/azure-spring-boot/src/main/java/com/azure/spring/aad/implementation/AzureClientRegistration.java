// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Set;

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
