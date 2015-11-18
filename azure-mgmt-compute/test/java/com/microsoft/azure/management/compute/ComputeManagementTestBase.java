package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.NetworkManagementClient;
import com.microsoft.azure.management.network.NetworkManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import com.microsoft.rest.credentials.UserTokenCredentials;

public abstract class ComputeManagementTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static StorageManagementClient storageManagementClient;
    protected static ComputeManagementClient computeManagementClient;
    protected static NetworkManagementClient networkManagementClient;

    public static void createClients() {
        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                System.getenv("arm.redirecturi"),
                null);
        resourceManagementClient = new ResourceManagementClientImpl(credentials);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        storageManagementClient = new StorageManagementClientImpl(credentials);
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        networkManagementClient = new NetworkManagementClientImpl(credentials);
        networkManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        computeManagementClient = new ComputeManagementClientImpl(credentials);
        computeManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }
}
