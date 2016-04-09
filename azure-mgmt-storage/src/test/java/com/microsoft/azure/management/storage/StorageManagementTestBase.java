package com.microsoft.azure.management.storage;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.AzureResourceAuthenticated;
import com.microsoft.azure.management.resources.implementation.AzureResource;
import com.microsoft.azure.management.storage.implementation.AzureStorage;

public abstract class StorageManagementTestBase {
    protected static AzureResourceAuthenticated resourceClient;
    protected static AzureStorageAuthenticated storageClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.secret"),
                null);

        resourceClient = AzureResource.authenticate(credentials); // TODO: subscription-id
        storageClient = AzureStorage.authenticate(credentials, System.getenv("arm.subscriptionid"));
        // resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        // storageManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
