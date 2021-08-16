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
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public UsernamePasswordCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    UsernamePasswordCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    UsernamePasswordCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
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
