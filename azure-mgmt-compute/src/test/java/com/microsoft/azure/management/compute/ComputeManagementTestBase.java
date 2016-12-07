package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ComputeManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static ComputeManager computeManager;
    protected static NetworkManager networkManager;
    protected static StorageManager storageManager;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        computeManager = ComputeManager
                .authenticate(restClient, System.getenv("subscription-id"));

        networkManager = NetworkManager
                .authenticate(restClient, System.getenv("subscription-id"));

        storageManager = StorageManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}
