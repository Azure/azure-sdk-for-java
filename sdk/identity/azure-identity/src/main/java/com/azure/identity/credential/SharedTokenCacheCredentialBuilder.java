// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

/**
 * Fluent credential builder for instantiating a {@link SharedTokenCacheCredential}.
 *
 * @see SharedTokenCacheCredential
 */
public class SharedTokenCacheCredentialBuilder extends CredentialBuilderBase<ManagedIdentityCredentialBuilder> {
    private String clientId;
    private String username;


    /**
     * Sets clientId
     *
     * @param clientId client id for application
     *
     * @return SharedTokenCacheCredentialBuilder
     * */
    public SharedTokenCacheCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets username
     *
     * @param username username for user account
     *
     * @return SharedTokenCacheCredentialBuilder
     * */
    public SharedTokenCacheCredentialBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * @return a {@link SharedTokenCacheCredentialBuilder} with the current configurations.
     */
    public SharedTokenCacheCredential build() {
        return new SharedTokenCacheCredential(username, clientId, identityClientOptions);

    }

}
