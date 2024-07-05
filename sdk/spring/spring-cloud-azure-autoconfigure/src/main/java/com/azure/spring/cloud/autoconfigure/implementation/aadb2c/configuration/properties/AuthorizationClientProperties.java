// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.List;

/**
 * Properties for an OAuth2 client.
 */
public class AuthorizationClientProperties {

    private List<String> scopes;

    private AuthorizationGrantType authorizationGrantType;

    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    public void setAuthorizationGrantType(AuthorizationGrantType authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
