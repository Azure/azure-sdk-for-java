// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;

/**
 * To provide an Azure service connection string from Azure Resource Manager (ARM).
 * @param <T> The Azure service type.
 */
public abstract class AbstractArmConnectionStringProvider<T> implements ConnectionStringProvider<T> {

    protected final AzureResourceManager azureResourceManager;
    protected final AzureResourceMetadata azureResourceMetadata;

    /**
     * Creates a new instance of {@link AbstractArmConnectionStringProvider}.
     * @param resourceManager the azure resource manager
     * @param resourceMetadata the azure resource metadata
     */
    public AbstractArmConnectionStringProvider(AzureResourceManager resourceManager,
                                               AzureResourceMetadata resourceMetadata) {
        this.azureResourceManager = resourceManager;
        this.azureResourceMetadata = resourceMetadata;
    }

}
