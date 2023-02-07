// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import static com.azure.identity.ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE;

/**
 * Fluent credential builder for instantiating a {@link WorkloadIdentityCredential}.
 *
 * @see WorkloadIdentityCredential
 */
public class WorkloadIdentityCredentialBuilder extends CredentialBuilderBase<WorkloadIdentityCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(WorkloadIdentityCredentialBuilder.class);
    private String clientId;

    /**
     * Creates an instance of a WorkloadIdentityCredentialBuilder.
     */
    public WorkloadIdentityCredentialBuilder() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
    }

    /**
     * Specifies the client ID of managed identity, when this credential is running
     * in Azure Kubernetes. If unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used.
     *
     * @param clientId the client ID
     * @return the WorkloadIdentityCredentialBuilder itself
     */
    public WorkloadIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Creates new {@link WorkloadIdentityCredential} with the configured options set.
     *
     * @return a {@link WorkloadIdentityCredential} with the current configurations.
     */
    public WorkloadIdentityCredential build() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone() : identityClientOptions.getConfiguration();

        String tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String federatedTokenFilePath = configuration.get(AZURE_FEDERATED_TOKEN_FILE);
        String azureAuthorityHost = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);

        return new WorkloadIdentityCredential(clientId, tenantId, federatedTokenFilePath,
                azureAuthorityHost, identityClientOptions.clone());
    }
}
