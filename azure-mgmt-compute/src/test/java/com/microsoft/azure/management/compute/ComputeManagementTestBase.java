package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.network.NetworkManagementClient;
import com.microsoft.azure.management.network.NetworkManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ComputeManagementTestBase {
    protected static ResourceManagementClientImpl resourceManagementClient;
    protected static StorageManagementClientImpl storageManagementClient;
    protected static ComputeManagementClientImpl computeManagementClient;
    protected static NetworkManagementClient networkManagementClient;

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
        networkManagementClient = new NetworkManagementClientImpl(credentials);
        networkManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        networkManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        computeManagementClient = new ComputeManagementClientImpl(credentials);
        computeManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        computeManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
