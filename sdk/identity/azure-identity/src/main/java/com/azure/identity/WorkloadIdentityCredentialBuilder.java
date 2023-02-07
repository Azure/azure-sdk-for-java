// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link WorkloadIdentityCredential}.
 *
 * @see WorkloadIdentityCredential
 */
public class WorkloadIdentityCredentialBuilder extends AadCredentialBuilderBase<WorkloadIdentityCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredentialBuilder.class);
    private String tokenFilePath;

    /**
     * Creates an instance of a WorkloadIdentityCredentialBuilder.
     */
    public WorkloadIdentityCredentialBuilder() { }


    /**
     * Configure the path to a file containing a Kubernetes service account token that authenticates the identity.
     * The file path is required to authenticate.
     *
     * @param tokenFilePath the path to the file containing the token to use for authentication.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public WorkloadIdentityCredentialBuilder tokenFilePath(String tokenFilePath) {
        this.tokenFilePath = tokenFilePath;
        return this;
    }

    /**
     * Creates new {@link WorkloadIdentityCredential} with the configured options set.
     *
     * @return a {@link WorkloadIdentityCredential} with the current configurations.
     */
    public WorkloadIdentityCredential build() {
        ValidationUtil.validate(this.getClass().getSimpleName(), LOGGER, "Client ID", clientId,
            "Tenant ID", tenantId, "Service Token File Path", tokenFilePath);

        return new WorkloadIdentityCredential(clientId, tenantId, tokenFilePath, identityClientOptions.clone());
    }
}
