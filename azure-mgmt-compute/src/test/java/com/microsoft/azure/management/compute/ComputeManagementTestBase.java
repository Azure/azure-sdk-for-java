package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.NetworkManagementClient;
import com.microsoft.azure.management.network.NetworkManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ComputeManagementTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static StorageManagementClient storageManagementClient;
    protected static ComputeManagementClient computeManagementClient;
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
