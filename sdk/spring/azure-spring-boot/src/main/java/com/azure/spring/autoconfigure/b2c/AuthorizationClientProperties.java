// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADAuthorizationGrantType;

import java.util.List;

/**
 * Properties for an oauth2 client.
 */
public class AuthorizationClientProperties {

    private List<String> scopes;

    private AADAuthorizationGrantType authorizationGrantType;

    public AADAuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    public void setAuthorizationGrantType(AADAuthorizationGrantType authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
