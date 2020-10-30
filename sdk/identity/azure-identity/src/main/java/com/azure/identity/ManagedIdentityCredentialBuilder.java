// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link ManagedIdentityCredential}.
 *
 * @see ManagedIdentityCredential
 */
public class ManagedIdentityCredentialBuilder extends CredentialBuilderBase<ManagedIdentityCredentialBuilder> {
    private String clientId;

    /**
     * Specifies the client ID of user assigned or system assigned identity.
     *
     * @param clientId the client ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Creates a new {@link ManagedIdentityCredential} with the current configurations.
     *
     * @return a {@link ManagedIdentityCredential} with the current configurations.
     */
    public ManagedIdentityCredential build() {
        return new ManagedIdentityCredential(clientId, identityClientOptions);
    }
}
