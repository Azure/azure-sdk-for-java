package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.network.implementation.api.NetworkManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ComputeManagementTestBase {
    protected static ResourceManagementClientImpl resourceManagementClient;
    protected static StorageManagementClientImpl storageManagementClient;
    protected static ComputeManagementClientImpl computeManagementClient;
    protected static NetworkManagementClientImpl networkManagementClient;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        RestClient.Builder restBuilder = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY);

        RestClient restClient = restBuilder.build();
        resourceManagementClient = new ResourceManagementClientImpl(restClient);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        storageManagementClient = new StorageManagementClientImpl(restClient);
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        networkManagementClient = new NetworkManagementClientImpl(restClient);
        networkManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        computeManagementClient = new ComputeManagementClientImpl(restClient);
        computeManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }
}
