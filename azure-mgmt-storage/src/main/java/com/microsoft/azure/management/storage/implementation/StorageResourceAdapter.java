package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceAdapter;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;

public class StorageResourceAdapter implements ResourceAdapter<StorageResourceAdapter> {
    private StorageManagementClientImpl client;
    private StorageAccounts storageAccounts;
    private Usages usages;

    private StorageResourceAdapter(ResourceManagementClientImpl resourceManagementClient) {
        this.client = new StorageManagementClientImpl(resourceManagementClient.getCredentials());
        this.storageAccounts = new StorageAccountsImpl(client, resourceManagementClient);
        this.usages = new UsagesImpl(client);
    }

    private static StorageResourceAdapter create(ResourceManagementClientImpl resourceManagementClient) {
        return new StorageResourceAdapter(resourceManagementClient);
    }

    public static class Builder implements ResourceAdapter.Builder<StorageResourceAdapter> {
        public StorageResourceAdapter create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
            return StorageResourceAdapter.create(resourceManagementClient);
        }
    }

    public StorageAccounts storageAccounts() {
        return storageAccounts;
    }

    public Usages usages() {
        return usages;
    }
}
