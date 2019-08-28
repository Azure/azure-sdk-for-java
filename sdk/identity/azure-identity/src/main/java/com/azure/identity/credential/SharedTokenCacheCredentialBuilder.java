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


    public SharedTokenCacheCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

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
