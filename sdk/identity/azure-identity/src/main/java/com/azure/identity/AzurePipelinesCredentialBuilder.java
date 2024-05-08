// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * The {@link AzurePipelinesCredentialBuilder} provides a fluent builder for {@link AzurePipelinesCredential}.
 */
public class AzurePipelinesCredentialBuilder extends AadCredentialBuilderBase<AzurePipelinesCredentialBuilder> {
    private String serviceConnectionId;

    /**
     * Sets the service connection id for the Azure Devops Pipeline service connection. The service connection id is
     * retrieved from the Serivce Connection in the portal.
     *
     * @param serviceConnectionId the service connection id for the Azure Devops Pipeline service connection.
     * @return the updated instance of the builder.
     */
    public AzurePipelinesCredentialBuilder serviceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
        return this;
    }

    /**
     * Builds an instance of the {@link AzurePipelinesCredential} with the current configurations.
     * @return an instance of the {@link AzurePipelinesCredential}.
     */
    public AzurePipelinesCredential build() {
        return new AzurePipelinesCredential(serviceConnectionId, identityClientOptions.clone());
    }
}
