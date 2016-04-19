package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.ResourceGroupContextStorage;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class StorageResourceConnector implements ResourceConnector<StorageResourceConnector> {
    private StorageManagementClientImpl client;
    private ResourceGroupContextStorage.StorageAccounts storageAccounts;
    private Usages usages;
    private ResourceGroup resourceGroup;

    private StorageResourceConnector(ServiceClientCredentials credentials, String subscriptionId,  ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
        this.client = new StorageManagementClientImpl(credentials);
        this.client.setSubscriptionId(subscriptionId);
    }

    private static StorageResourceConnector create(ServiceClientCredentials credentials,  String subscriptionId, ResourceGroup resourceGroup) {
        return new StorageResourceConnector(credentials, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<StorageResourceConnector> {
        public StorageResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
            return StorageResourceConnector.create(credentials, subscriptionId, resourceGroup);
        }
    }

    public ResourceGroupContextStorage.StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            this.storageAccounts = new ResourceGroupContextImpl(resourceGroup)
                    .storageAccounts(storageAccountsCore());
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
        ResourceManagementClientImpl resourceClient = new ResourceManagementClientImpl(credentials());
        resourceClient.setSubscriptionId(subscriptionId());
        StorageAccounts storageAccountsCore = new StorageAccountsImpl(this.client, new ResourceGroupsImpl(resourceClient));
        return storageAccountsCore;
    }

    private ServiceClientCredentials credentials() {
        return this.client.getCredentials();
    }

    private String subscriptionId() {
        return this.client.getSubscriptionId();
    }
}
