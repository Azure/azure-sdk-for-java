// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * The {@link AzurePipelinesServiceConnectionCredentialBuilder} provides a fluent builder for {@link AzurePipelinesServiceConnectionCredential}.
 */
public class AzurePipelinesServiceConnectionCredentialBuilder extends AadCredentialBuilderBase<AzurePipelinesServiceConnectionCredentialBuilder> {
    private String serviceConnectionId;

    /**
     * Sets the service connection id for the Azure Devops Pipeline service connection. The service connection id is
     * retrieved from the Serivce Connection in the portal.
     *
     * @param serviceConnectionId the service connection id for the Azure Devops Pipeline service connection.
     * @return the updated instance of the builder.
     */
    public AzurePipelinesServiceConnectionCredentialBuilder serviceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
        return this;
    }

    /**
     * Builds an instance of the {@link AzurePipelinesServiceConnectionCredential} with the current configurations.
     * @return an instance of the {@link AzurePipelinesServiceConnectionCredential}.
     */
    public AzurePipelinesServiceConnectionCredential build() {
        return new AzurePipelinesServiceConnectionCredential(serviceConnectionId, identityClientOptions.clone());
    }
}
