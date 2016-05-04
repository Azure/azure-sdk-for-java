package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseAuthImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class StorageManager {
    private final RestClient restClient;
    private final String subscriptionId;
    // The service managers
    private ResourceManager resourceClient;
    // The sdk clients
    private StorageManagementClientImpl storageManagementClient;
    // The collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    public static AzureConfigureBaseAuthImpl<StorageManager> configure() {
        return new AzureConfigureBaseAuthImpl<StorageManager>() {
            public StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
                buildRestClient(credentials);
                return StorageManager.authenticate(this.restClient, subscriptionId);
            }
        };
    }

    public static StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new StorageManager(credentials, subscriptionId);
    }

    public static StorageManager authenticate(RestClient restClient, String subscriptionId) {
        return new StorageManager(restClient, subscriptionId);
    }

    private StorageManager(ServiceClientCredentials credentials, String subscriptionId) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
        this.subscriptionId = subscriptionId;
    }

    private StorageManager(RestClient restClient, String subscriptionId) {
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

    private ResourceManager resourceClient() {
        if (restClient == null) {
            resourceClient = ResourceManager
                    .authenticate(restClient)
                    .useSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
