package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureStorageManager {
    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static Authenticated authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureAuthenticatedImpl(credentials, subscriptionId);
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        Authenticated authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    public interface Authenticated {
        StorageAccounts storageAccounts();
        Usages usages();
    }
}
