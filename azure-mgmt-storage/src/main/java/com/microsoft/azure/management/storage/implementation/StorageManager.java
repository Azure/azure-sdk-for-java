package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class StorageManager {
    private final RestClient restClient;

    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static StorageManager authenticate(ServiceClientCredentials credentials) {
        return new StorageManager(credentials);
    }

    public static StorageManager authenticate(RestClient restClient) {
        return new StorageManager(restClient);
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        StorageManager authenticate(ServiceClientCredentials credentials);
    }

    public interface Subscription {
        StorageAccounts storageAccounts();
        Usages usages();
    }

    private StorageManager(ServiceClientCredentials credentials) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
    }

    private StorageManager(RestClient restClient) {
        this.restClient = restClient;
    }

    public StorageManager.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(restClient, subscriptionId);
    }
}
