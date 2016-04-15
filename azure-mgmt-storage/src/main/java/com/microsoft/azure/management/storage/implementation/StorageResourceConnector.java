package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;

public class StorageResourceConnector implements ResourceConnector<StorageResourceConnector> {
    private StorageManagementClientImpl client;
    private StorageAccounts storageAccounts;
    private Usages usages;

    private StorageResourceConnector(ResourceManagementClientImpl resourceManagementClient) {
        this.client = new StorageManagementClientImpl(resourceManagementClient.getCredentials());
        this.storageAccounts = new StorageAccountsImpl(client, resourceManagementClient);
        this.usages = new UsagesImpl(client);
    }

    private static StorageResourceConnector create(ResourceManagementClientImpl resourceManagementClient) {
        return new StorageResourceConnector(resourceManagementClient);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
            return StorageResourceConnector.create(resourceManagementClient);
        }
    }

    public StorageAccounts storageAccounts() {
        return storageAccounts;
    }

    public Usages usages() {
        return usages;
    }
}
