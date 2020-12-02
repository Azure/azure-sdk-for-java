// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.List;

public class DefaultClient {

    private final ClientRegistration client;
    private final List<String> scopes;

    public DefaultClient(ClientRegistration client, List<String> scopes) {
        this.client = client;
        this.scopes = scopes;
    }

    public ClientRegistration client() {
        return client;
    }

    public List<String> scope() {
        return scopes;
    }

    public List<String> scopes() {
        return scopes;
    }
}
