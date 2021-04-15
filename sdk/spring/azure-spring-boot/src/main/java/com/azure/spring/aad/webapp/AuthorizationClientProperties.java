// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.List;

/**
 * Properties for an oauth2 client.
 */
public class AuthorizationClientProperties {

    private List<String> scopes;
    private boolean onDemand = false;
    // TODO (rujche): breaking change, need to update sample and changelog.md.
    private AuthorizationGrantType authorizationGrantType;

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }

    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    public void setAuthorizationGrantType(AuthorizationGrantType authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }
}
