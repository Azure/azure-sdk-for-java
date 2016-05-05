package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.rest.RestClient;

public class StorageResourceConnector extends ResourceConnectorBase<StorageResourceConnector> {
    private StorageManager storageClient;
    private StorageAccounts.InGroup storageAccounts;

    private StorageResourceConnector(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        super(restClient, subscriptionId, resourceGroup);
    }

    private static StorageResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        return new StorageResourceConnector(restClient, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
            return StorageResourceConnector.create(restClient, subscriptionId, resourceGroup);
        }
    }

    public StorageAccounts.InGroup storageAccounts() {
        if (storageAccounts == null) {
            this.storageAccounts = new StorageAccountsInGroupImpl(storageClient().storageAccounts(), resourceGroup);
        }
        return storageAccounts;
    }

    private StorageManager storageClient() {
        if (storageClient == null) {
            storageClient = StorageManager
                    .authenticate(restClient, subscriptionId);
        }
        return storageClient;
    }
}
