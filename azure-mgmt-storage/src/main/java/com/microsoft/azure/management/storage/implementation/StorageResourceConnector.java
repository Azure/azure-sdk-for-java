package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.ResourceGroupContext;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class StorageResourceConnector implements ResourceConnector<StorageResourceConnector> {
    private StorageManagementClientImpl client;
    private ResourceGroupContext.StorageAccounts storageAccounts;
    private Usages usages;

    private StorageResourceConnector(ServiceClientCredentials credentials,  ResourceGroup resourceGroup) {
        this.client = new StorageManagementClientImpl(credentials);
        this.storageAccounts = new StorageAccountsWithGroupContextImpl(client, resourceGroup);
        this.usages = new UsagesImpl(client);
    }

    private static StorageResourceConnector create(ServiceClientCredentials credentials,  ResourceGroup resourceGroup) {
        return new StorageResourceConnector(credentials, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(ServiceClientCredentials credentials, ResourceGroup resourceGroup) {
            return StorageResourceConnector.create(credentials, resourceGroup);
        }
    }

    public ResourceGroupContext.StorageAccounts storageAccounts() {
        return storageAccounts;
    }

    public Usages usages() {
        return usages;
    }
}
