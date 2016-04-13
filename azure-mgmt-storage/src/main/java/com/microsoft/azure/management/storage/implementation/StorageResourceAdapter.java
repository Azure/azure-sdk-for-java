package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceAdapter;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class StorageResourceAdapter implements ResourceAdapter<StorageResourceAdapter> {
    private StorageManagementClientImpl client;
    private StorageAccounts storageAccounts;
    private Usages usages;

    private StorageResourceAdapter(ServiceClientCredentials credentials) {
        this.client = new StorageManagementClientImpl(credentials);
        this.storageAccounts = new StorageAccountsImpl(client);
        this.usages = new UsagesImpl(client);
    }

    private static StorageResourceAdapter create(ServiceClientCredentials credentials) {
        return new StorageResourceAdapter(credentials);
    }

    public static class Builder implements ResourceAdapter.Builder<StorageResourceAdapter> {
        public StorageResourceAdapter create(ServiceClientCredentials credentials, String resourceGroupName) {
            return StorageResourceAdapter.create(credentials);
        }
    }

    public StorageAccounts storageAccounts() {
        return storageAccounts;
    }

    public Usages usages() {
        return usages;
    }
}
