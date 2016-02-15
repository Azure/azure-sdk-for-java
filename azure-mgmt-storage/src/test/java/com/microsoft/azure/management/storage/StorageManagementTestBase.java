package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class StorageManagementTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static StorageManagementClient storageManagementClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.secret"),
                null);
        resourceManagementClient = new ResourceManagementClientImpl(credentials);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        storageManagementClient = new StorageManagementClientImpl(credentials);
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        storageManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
