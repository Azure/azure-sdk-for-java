package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class StorageResourceConnector extends ResourceConnectorBase<StorageResourceConnector> {
    private StorageManagementClientImpl client;
    private StorageAccounts.InGroup storageAccounts;
    private Usages usages;

    private StorageResourceConnector(ServiceClientCredentials credentials, String subscriptionId,  ResourceGroup resourceGroup) {
        super(credentials, subscriptionId, resourceGroup);
        constructClient();
    }

    private static StorageResourceConnector create(ServiceClientCredentials credentials,  String subscriptionId, ResourceGroup resourceGroup) {
        return new StorageResourceConnector(credentials, subscriptionId, resourceGroup);
    }

    private void constructClient() {
        client = new StorageManagementClientImpl(credentials);
        client.setSubscriptionId(subscriptionId);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
            return StorageResourceConnector.create(credentials, subscriptionId, resourceGroup);
        }
    }

    public StorageAccounts.InGroup storageAccounts() {
        if (storageAccounts == null) {
            this.storageAccounts = new StorageAccountsInGroupImpl(storageAccountsCore(), resourceGroup);
        }
        return storageAccounts;
    }

    public Usages usages() {
        if (usages == null) {
            this.usages = new UsagesImpl(client);
        }
        return usages;
    }

    private StorageAccounts storageAccountsCore() {
        StorageAccounts storageAccountsCore = new StorageAccountsImpl(this.client.storageAccounts(), resourceGroups());
        return storageAccountsCore;
    }
}
