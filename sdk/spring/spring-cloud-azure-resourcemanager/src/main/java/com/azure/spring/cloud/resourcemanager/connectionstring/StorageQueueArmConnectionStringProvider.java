// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.spring.cloud.resourcemanager.implementation.crud.StorageAccountCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.service.AzureServiceType;

/**
 * A connection string provider reads Storage Queue connection string from Azure Resource Manager.
 * // TODO (xiada): Do blob, queue share the same connection string?
 */
public class StorageQueueArmConnectionStringProvider extends AbstractArmConnectionStringProvider<AzureServiceType.StorageQueue> {

    private final String accountName;

    /**
     * Creates a new instance of {@link StorageQueueArmConnectionStringProvider}.
     * @param resourceManager the azure resource manager
     * @param resourceMetadata the azure resource metadata
     * @param accountName the accountName
     */
    public StorageQueueArmConnectionStringProvider(AzureResourceManager resourceManager,
                                                   AzureResourceMetadata resourceMetadata,
                                                   String accountName) {
        super(resourceManager, resourceMetadata);
        this.accountName = accountName;
    }

    public String getConnectionString() {
        return new StorageAccountCrud(this.azureResourceManager, this.azureResourceMetadata)
            .get(this.accountName)
            .getKeys()
            .stream()
            .findFirst()
            .map(key -> ResourceManagerUtils.getStorageConnectionString(this.accountName, key.value(),
                                                                        this.azureResourceManager.storageAccounts().manager().environment()))
            .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    @Override
    public AzureServiceType.StorageQueue getServiceType() {
        return AzureServiceType.STORAGE_QUEUE;
    }
}
