package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

class AzureSubscriptionImpl implements AzureStorageManager.Subscription {

    private ServiceClientCredentials credentials;
    private String subscriptionId;
    private StorageManagementClientImpl storageManagementClient;

    AzureSubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public StorageAccounts storageAccounts() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(credentials);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return new StorageAccountsImpl(storageManagementClient);
    }

    @Override
    public Usages usages() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(credentials);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return new UsagesImpl(storageManagementClient);
    }
}
