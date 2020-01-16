// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link UsernamePasswordCredential}.
 *
 * @see UsernamePasswordCredential
 */
public class UsernamePasswordCredentialBuilder extends AadCredentialBuilderBase<UsernamePasswordCredentialBuilder> {
    private String username;
    private String password;

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
     * Creates a new {@link UsernamePasswordCredential} with the current configurations.
     *
     * @return a {@link UsernamePasswordCredential} with the current configurations.
     */
    public UsernamePasswordCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("username", username);
                put("password", password);
            }});
        return new UsernamePasswordCredential(clientId, tenantId, username, password, identityClientOptions);
    }
}
