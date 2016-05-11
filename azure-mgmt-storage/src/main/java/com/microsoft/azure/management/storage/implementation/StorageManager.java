package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class StorageManager {
    private final StorageManagementClientImpl storageManagementClient;
    // Dependent managers
    private ResourceManager resourceManager;
    // Collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    public static Configurable configure() {
        return new StorageManager.ConfigurableImpl();
    }

    public static StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new StorageManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    public static StorageManager authenticate(RestClient restClient, String subscriptionId) {
        return new StorageManager(restClient, subscriptionId);
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
        StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return StorageManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private StorageManager(RestClient restClient, String subscriptionId) {
        storageManagementClient = new StorageManagementClientImpl(restClient);
        storageManagementClient.setSubscriptionId(subscriptionId);
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
    }

    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(storageManagementClient.storageAccounts(), resourceManager.resourceGroups());
        }
        return storageAccounts;
    }

    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient);
        }
        return storageUsages;
    }

}
