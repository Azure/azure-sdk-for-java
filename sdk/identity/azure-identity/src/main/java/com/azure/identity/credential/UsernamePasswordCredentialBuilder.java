// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link UsernamePasswordCredential}.
 *
 * @see UsernamePasswordCredential
 */
public class UsernamePasswordCredentialBuilder extends AadCredentialBuilderBase<UsernamePasswordCredentialBuilder> {
    private String tenantId;
    private String username;
    private String password;

    /**
     * Sets the tenant ID of the application.
     * @param tenantId the tenant ID of the application.
     * @return the UserCredentialBuilder itself
     */
    public UsernamePasswordCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the username of the user.
     * @param username the username of the user
     * @return the UserCredentialBuilder itself
     */
    public UsernamePasswordCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password of the user.
     * @param password the password of the user
     * @return the UserCredentialBuilder itself
     */
    public UsernamePasswordCredentialBuilder password(String password) {
        this.password = password;
        return this;
    }

    /**
     * @return a {@link UsernamePasswordCredential} with the current configurations.
     */
    public UsernamePasswordCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("username", username);
                put("password", password);
            }});
        return new UsernamePasswordCredential(tenantId, clientId, username, password, identityClientOptions);
    }
}
