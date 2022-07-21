// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.List;

/**
 * Properties for an oauth2 client.
 */
public class AuthorizationClientProperties {

    private List<String> scopes;

    private AuthorizationGrantType authorizationGrantType;

    private ClientAuthenticationMethod clientAuthenticationMethod;

    /**
     * Gets the authorization grant type.
     *
     * @return the authorization grant type
     */
    public AuthorizationGrantType getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    /**
     * Sets the authorization grant type.
     *
     * @param authorizationGrantType the authorization grant type
     */
    public void setAuthorizationGrantType(AuthorizationGrantType authorizationGrantType) {
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
     * Gets the client authentication method.
     *
     * @return the client authentication method
     */
    public ClientAuthenticationMethod getClientAuthenticationMethod() {
        return clientAuthenticationMethod;
    }

    /**
     * Sets the client authentication method.
     *
     * @param clientAuthenticationMethod the client authentication method
     */
    public void setClientAuthenticationMethod(ClientAuthenticationMethod clientAuthenticationMethod) {
        this.clientAuthenticationMethod = clientAuthenticationMethod;
    }
}
