// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;

/**
 * To provide an Azure service connection string from Azure Resource Manager (ARM).
 * @param <T> The Azure service type.
 */
public abstract class ArmConnectionStringProvider<T> implements ServiceConnectionStringProvider<T> {

    private final AzureResourceManager azureResourceManager;
    private final AzureResourceMetadata azureResourceMetadata;

    /**
     * Creates a new instance of {@link ArmConnectionStringProvider}.
     * @param resourceManager the azure resource manager
     * @param resourceMetadata the azure resource metadata
     */
    public ArmConnectionStringProvider(AzureResourceManager resourceManager,
                                       AzureResourceMetadata resourceMetadata) {
        this.azureResourceManager = resourceManager;
        this.azureResourceMetadata = resourceMetadata;
    }

    /**
     * Get the {@link AzureResourceManager}.
     * @return the Azure resource manager.
     */
    AzureResourceManager getAzureResourceManager() {
        return azureResourceManager;
    }

    /**
     * Get the {@link AzureResourceMetadata}.
     * @return the metadata for the Azure resource.
     */
    AzureResourceMetadata getAzureResourceMetadata() {
        return azureResourceMetadata;
    }
}
