package com.microsoft.azure.management.storage;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;

public abstract class StorageManagementTestBase {
    protected static ResourceManager resourceClient;
    protected static StorageManager storageClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.secret"),
                null);

        resourceClient = ResourceManager
                .authenticate(credentials)
                .useSubscription(System.getenv("arm.subscriptionid"));

        storageClient = StorageManager.
                authenticate(credentials, System.getenv("arm.subscriptionid"));

        // resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        // storageManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
