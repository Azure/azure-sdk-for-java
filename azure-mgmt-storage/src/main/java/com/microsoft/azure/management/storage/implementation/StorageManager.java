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

/**
 * Entry point to Azure storage resource management.
 */
public final class StorageManager {
    private final StorageManagementClientImpl storageManagementClient;

    // Dependent managers
    private final ResourceManager resourceManager;

    // Collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    /**
     * Get a Configurable instance that can be used to create StorageManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new StorageManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new StorageManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static StorageManager authenticate(RestClient restClient, String subscriptionId) {
        return new StorageManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing storage management API entry points that work across subscriptions
         */
        StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public StorageManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return StorageManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private StorageManager(RestClient restClient, String subscriptionId) {
        storageManagementClient = new StorageManagementClientImpl(restClient);
        storageManagementClient.withSubscriptionId(subscriptionId);
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
    }

    /**
     * @return the storage account management API entry point
     */
    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(storageManagementClient.storageAccounts(), resourceManager.resourceGroups());
        }
        return storageAccounts;
    }

    /**
     * @return the storage resource usage management API entry point
     */
    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient);
        }
        return storageUsages;
    }
}
