package com.microsoft.azure.management.batch;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class BatchManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static BatchManager batchManager;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        batchManager = BatchManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}
