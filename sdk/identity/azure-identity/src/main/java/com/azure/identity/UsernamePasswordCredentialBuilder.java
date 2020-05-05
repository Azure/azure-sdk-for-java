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
     * Sets whether to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is false by default.
     *
     * @param allowUnencryptedCache whether to use an unprotected file for cache storage.
     *
     * @return An updated instance of this builder with the unprotected token cache setting set as specified.
     */
    public UsernamePasswordCredentialBuilder allowUnencryptedCache(boolean allowUnencryptedCache) {
        this.identityClientOptions.allowUnencryptedCache(allowUnencryptedCache);
        return this;
    }

    /**
     * Sets whether to enable using the shared token cache. This is disabled by default.
     *
     * @param enabled whether to enabled using the shared token cache.
     *
     * @return An updated instance of this builder with if the shared token cache enabled specified.
     */
    public UsernamePasswordCredentialBuilder enablePersistentCache(boolean enabled) {
        this.identityClientOptions.enablePersistentCache(enabled);
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
