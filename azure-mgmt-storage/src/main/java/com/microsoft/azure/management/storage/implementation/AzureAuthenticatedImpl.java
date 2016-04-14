package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureBaseImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

class AzureAuthenticatedImpl extends AzureBaseImpl<AzureStorageManager.Authenticated>
        implements AzureStorageManager.Authenticated {
    private ServiceClientCredentials credentials;
    private String subscriptionId;

    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public StorageAccounts storageAccounts() {
        return new StorageAccountsImpl(storageManagementClient(), resourceManagementClient());
    }

    @Override
    public Usages usages() {
        return new UsagesImpl(storageManagementClient());
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(credentials);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }

    private StorageManagementClientImpl storageManagementClient() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(credentials);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return storageManagementClient;
    }
}
