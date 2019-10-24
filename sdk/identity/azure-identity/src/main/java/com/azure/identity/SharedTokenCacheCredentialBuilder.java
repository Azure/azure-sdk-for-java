// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link SharedTokenCacheCredential}.
 *
 * @see SharedTokenCacheCredential
 */
public class SharedTokenCacheCredentialBuilder extends CredentialBuilderBase<ManagedIdentityCredentialBuilder> {
    private String clientId;
    private String username;

    /**
     * Sets the client ID for the application.
     *
     * @param clientId The client ID for the application.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     * */
    public SharedTokenCacheCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the username for the account.
     *
     * @param username The username for the account.
     *
     * @return The updated SharedTokenCacheCredentialBuilder object.
     * */
    public SharedTokenCacheCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Creates a new {@link SharedTokenCacheCredentialBuilder} with the current configurations.
     *
     * @return a {@link SharedTokenCacheCredentialBuilder} with the current configurations.
     */
    public SharedTokenCacheCredential build() {
        return new SharedTokenCacheCredential(username, clientId, identityClientOptions);
    }
}
