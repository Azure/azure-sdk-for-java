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

    /**
     * Whether authorization is on demand.
     *
     * @return whether authorization is on demand
     * @deprecated The AuthorizationGrantType of on-demand clients should be authorization_code.
     * Set oauth client AuthorizationGrantType to authorization_code, which means it's on-demand.
     */
    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "The AuthorizationGrantType of on-demand clients should be authorization_code.",
        replacement = "Set oauth client AuthorizationGrantType to authorization_code, which means it's on-demand.")
    public boolean isOnDemand() {
        return onDemand;
    }

    /**
     * Sets whether authorization is on demand.
     *
     * @param onDemand whether authorization is on demand
     * @deprecated The AuthorizationGrantType of on-demand clients should be authorization_code.
     * Set oauth client AuthorizationGrantType to authorization_code, which means it's on-demand.
     */
    @Deprecated
    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }
}
