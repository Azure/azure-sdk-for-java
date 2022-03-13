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

    /**
     * Gets the authorization grant type.
     *
     * @return the authorization grant type
     */
    public AADAuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    /**
     * Sets the authorization grant type.
     *
     * @param authorizationGrantType the authorization grant type
     */
    public void setAuthorizationGrantType(AADAuthorizationGrantType authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    /**
     * Sets the list of scopes.
     *
     * @param scopes the list of scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Gets the list of scopes.
     *
     * @return the list of scopes
     */
    public List<String> getScopes() {
        return scopes;
    }
}
