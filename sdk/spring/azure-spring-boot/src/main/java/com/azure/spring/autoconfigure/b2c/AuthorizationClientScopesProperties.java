// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import java.util.Set;

/**
 * Properties for an oauth2 client.
 */
public class AuthorizationClientScopesProperties {

    private Set<String> scopes;

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
