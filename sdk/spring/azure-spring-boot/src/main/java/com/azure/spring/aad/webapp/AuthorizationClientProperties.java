// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

import java.util.List;

/**
 * Properties for an oauth2 client.
 */
public class AuthorizationClientProperties {

    private List<String> scopes;

    private boolean onDemand = false;

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

    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "The AuthorizationGrantType of on-demand clients should be authorization_code.",
        replacement = "Set oauth client AuthorizationGrantType to authorization_code, which means it's on-demand.")
    public boolean isOnDemand() {
        return onDemand;
    }

    @Deprecated
    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }
}
