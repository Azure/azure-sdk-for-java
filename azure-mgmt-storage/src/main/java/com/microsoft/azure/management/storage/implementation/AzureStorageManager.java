package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.AzureBase;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureStorageManager {
    public static Authenticated authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureAuthenticatedImpl(credentials, subscriptionId);
    }

    public interface Authenticated extends AzureBase<Authenticated> {
        StorageAccounts storageAccounts();
        Usages usages();
    }
}
