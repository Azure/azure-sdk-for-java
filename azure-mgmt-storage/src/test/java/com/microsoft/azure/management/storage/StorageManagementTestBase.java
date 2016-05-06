package com.microsoft.azure.management.storage;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class StorageManagementTestBase {
    protected static ResourceManager resourceClient;
    protected static StorageManager storageClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.secret"),
                null);

        RestClient restClient = new RestClient.Builder("https://management.azure.com")
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .build();

        resourceClient = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("arm.subscriptionid"));

        storageClient = StorageManager
                .authenticate(restClient, System.getenv("arm.subscriptionid"));
    }
}
