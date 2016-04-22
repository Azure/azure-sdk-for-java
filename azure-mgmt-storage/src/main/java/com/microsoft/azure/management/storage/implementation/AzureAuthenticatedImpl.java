package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureBaseImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

class AzureAuthenticatedImpl extends AzureBaseImpl<AzureStorageManager.Authenticated>
        implements AzureStorageManager.Authenticated {
    private ServiceClientCredentials credentials;
    private String subscriptionId;
    // The sdk clients
    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;
    // The collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(storageManagementClient(), resourceGroupsCore());
        }
        return storageAccounts;
    }

    @Override
    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient());
        }
        return storageUsages;
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

    private ResourceGroups resourceGroupsCore() {
        return new ResourceGroupsImpl(resourceManagementClient());
    }
}
