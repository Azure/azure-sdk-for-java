// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link SharedTokenCacheCredential}.
 *
 * @see SharedTokenCacheCredential
 */
public class ReadOnlySharedTokenCacheCredentialBuilder extends AadCredentialBuilderBase<ReadOnlySharedTokenCacheCredentialBuilder> {
    private String username;

    /**
     * Sets the username for the account.
     *
     * @param username The username for the account.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     */
    public ReadOnlySharedTokenCacheCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Creates a new {@link ReadOnlySharedTokenCacheCredentialBuilder} with the current configurations.
     *
     * @return a {@link ReadOnlySharedTokenCacheCredentialBuilder} with the current configurations.
     */
    public ReadOnlySharedTokenCacheCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("cacheFileLocation", cacheFileLocation);
            }});
        if (cacheFileLocation != null) {
            identityClientOptions.setPersistenceSettings(cacheFileLocation, keychainService, keychainAccount,
                    keyringName, keyringItemSchema, keyringItemName, attributes, useUnprotectedFileOnLinux);
        }
        return new ReadOnlySharedTokenCacheCredential(username, clientId, tenantId, identityClientOptions);
    }
}
