package com.microsoft.azure.management.storage.implementation;


import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;

class AzureSubscriptionImpl  implements StorageManager.Subscription {
    private final RestClient restClient;
    private final String subscriptionId;
    // The sdk clients
    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;
    // The collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    public AzureSubscriptionImpl(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(storageManagementClient().storageAccounts(), resourceGroups());
        }
        return storageAccounts;
    }

    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient());
        }
        return storageUsages;
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }

    private StorageManagementClientImpl storageManagementClient() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(restClient);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return storageManagementClient;
    }

    private ResourceGroups resourceGroups() {
        return new ResourceGroupsImpl(resourceManagementClient());
    }
}
