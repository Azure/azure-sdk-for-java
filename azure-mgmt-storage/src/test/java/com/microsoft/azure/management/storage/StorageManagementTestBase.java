package com.microsoft.azure.management.storage;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.AzureResourceManager;
import com.microsoft.azure.management.storage.implementation.AzureStorageManager;

public abstract class StorageManagementTestBase {
    protected static AzureResourceAuthenticated resourceClient;
    protected static AzureStorageAuthenticated storageClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.secret"),
                null);

        resourceClient = AzureResourceManager.authenticate(credentials); // TODO: subscription-id
        storageClient = AzureStorageManager.authenticate(credentials, System.getenv("arm.subscriptionid"));
        // resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        // storageManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
