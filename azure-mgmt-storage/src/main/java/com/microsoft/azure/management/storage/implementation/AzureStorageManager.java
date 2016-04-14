package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureStorageManager {
    public static AzureStorageManager.Subscription authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureSubscriptionImpl(credentials, subscriptionId);
    }

    public interface Subscription {
        StorageAccounts storageAccounts();
        Usages usages();
    }
}
