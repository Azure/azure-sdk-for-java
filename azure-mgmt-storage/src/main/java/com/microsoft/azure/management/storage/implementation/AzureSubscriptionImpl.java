package com.microsoft.azure.management.storage.implementation;


import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;

class AzureSubscriptionImpl  implements StorageManager.Subscription {
    private final RestClient restClient;
    private final String subscriptionId;
    // The service managers
    private ResourceManager.Subscription resourceClient;
    // The sdk clients
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
            storageAccounts = new StorageAccountsImpl(storageManagementClient().storageAccounts(), resourceClient().resourceGroups());
        }
        return storageAccounts;
    }

    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient());
        }
        return storageUsages;
    }

    private StorageManagementClientImpl storageManagementClient() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(restClient);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return storageManagementClient;
    }

    private ResourceManager.Subscription resourceClient() {
        if (restClient == null) {
            resourceClient = ResourceManager
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
