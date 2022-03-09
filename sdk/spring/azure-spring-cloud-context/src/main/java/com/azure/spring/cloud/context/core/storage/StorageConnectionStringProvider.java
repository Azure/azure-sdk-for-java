// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.storage;

import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;

/**
 * A provider that holds the storage account connection string.
 */
public class StorageConnectionStringProvider {

    private final String connectionString;

    /**
     * Creates a new instance of {@link StorageConnectionStringProvider}.
     *
     * @param storageAccount The Storage account.
     */
    public StorageConnectionStringProvider(StorageAccount storageAccount) {
        this.connectionString = buildConnectionString(storageAccount);
    }

    /**
     * Creates a new instance of {@link StorageConnectionStringProvider}.
     *
     * @param accountName The Storage account name.
     * @param accountKey The Storage account key.
     * @param environment The Azure environment.
     */
    public StorageConnectionStringProvider(String accountName, String accountKey, AzureEnvironment environment) {
        this.connectionString = ResourceManagerUtils.getStorageConnectionString(accountName, accountKey, environment);
    }

    private static String buildConnectionString(StorageAccount storageAccount) {
        return storageAccount.getKeys()
                             .stream()
                             .findFirst()
                             .map(key -> ResourceManagerUtils.getStorageConnectionString(storageAccount.name(),
                                 key.value(),
                                 storageAccount.manager().environment()))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }

    /**
     * Gets the connection string.
     *
     * @return The connection string.
     */
    public String getConnectionString() {
        return connectionString;
    }
}
