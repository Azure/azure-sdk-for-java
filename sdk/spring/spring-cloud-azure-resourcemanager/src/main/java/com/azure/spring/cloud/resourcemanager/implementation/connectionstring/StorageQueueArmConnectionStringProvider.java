// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.resourcemanager.implementation.crud.StorageAccountCrud;

/**
 * A connection string provider reads Storage Queue connection string from Azure Resource Manager.
 * // TODO (xiada): Do blob, queue share the same connection string?
 */
public class StorageQueueArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.StorageQueue> {

    private final String accountName;
    private final StorageAccountCrud storageAccountCrud;

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
        this.storageAccountCrud = new StorageAccountCrud(resourceManager, resourceMetadata);
    }

    @Override
    public String getConnectionString() {
        return this.storageAccountCrud
            .get(this.accountName)
            .getKeys()
            .stream()
            .findFirst()
            .map(key -> ResourceManagerUtils.getStorageConnectionString(this.accountName, key.value(),
                                                                        this.getAzureResourceManager()
                                                                            .storageAccounts()
                                                                            .manager()
                                                                            .environment()))
            .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    @Override
    public AzureServiceType.StorageQueue getServiceType() {
        return AzureServiceType.STORAGE_QUEUE;
    }
}
