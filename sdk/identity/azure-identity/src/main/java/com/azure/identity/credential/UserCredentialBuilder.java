// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

/**
 * Fluent credential builder for instantiating a {@link UserCredential}.
 *
 * @see UserCredential
 */
public class UserCredentialBuilder extends AadCredentialBuilderBase<UserCredentialBuilder> {
    private String username;
    private String password;

    /**
     * Sets the username of the user.
     * @param username the username of the user
     * @return the UserCredentialBuilder itself
     */
    public UserCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password of the user.
     * @param password the password of the user
     * @return the UserCredentialBuilder itself
     */
    public UserCredentialBuilder password(String password) {
        this.password = password;
        return this;
    }

    /**
     * @return a {@link UserCredential} with the current configurations.
     */
    public UserCredential build() {
        return new UserCredential(tenantId, clientId, username, password, identityClientOptions);
    }
}
