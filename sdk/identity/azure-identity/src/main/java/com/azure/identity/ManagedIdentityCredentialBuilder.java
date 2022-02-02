// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;

/**
 * Fluent credential builder for instantiating a {@link ManagedIdentityCredential}.
 *
 * @see ManagedIdentityCredential
 */
public class ManagedIdentityCredentialBuilder extends CredentialBuilderBase<ManagedIdentityCredentialBuilder> {
    private String clientId;
    private String resourceId;
    private final ClientLogger logger = new ClientLogger(ManagedIdentityCredentialBuilder.class);

    /**
     * Specifies the client ID of user assigned or system assigned identity.
     *
     * Only one of clientId and resourceId can be specified.
     *
     * @param clientId the client ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specifies the resource ID of a user assigned or system assigned identity.
     *
     * Only one of clientId and resourceId can be specified.
     *
     * @param resourceId the resource ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Creates a new {@link ManagedIdentityCredential} with the current configurations.
     *
     * @return a {@link ManagedIdentityCredential} with the current configurations.
     */
    public ManagedIdentityCredential build() {
        if (clientId != null && resourceId != null) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Only one of clientId and resourceId can be specified."));
        }

        return new ManagedIdentityCredential(clientId, resourceId, identityClientOptions);
    }
}
