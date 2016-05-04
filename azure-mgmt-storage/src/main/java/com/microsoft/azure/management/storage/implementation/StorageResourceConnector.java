package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class StorageResourceConnector extends ResourceConnectorBase<StorageResourceConnector> {
    StorageManager storageClient;
    private StorageAccounts.InGroup storageAccounts;

    private StorageResourceConnector(ServiceClientCredentials credentials, String subscriptionId,  ResourceGroup resourceGroup) {
        super(credentials, subscriptionId, resourceGroup);
    }

    private static StorageResourceConnector create(ServiceClientCredentials credentials,  String subscriptionId, ResourceGroup resourceGroup) {
        return new StorageResourceConnector(credentials, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
            return StorageResourceConnector.create(credentials, subscriptionId, resourceGroup);
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
                    .authenticate(this.credentials(), this.subscriptionId());
        }
        return storageClient;
    }
}
