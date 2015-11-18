package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.rest.credentials.UserTokenCredentials;

public abstract class StorageManagementTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static StorageManagementClient storageManagementClient;

    public static void createClients() {
        resourceManagementClient = new ResourceManagementClientImpl(
                new UserTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.username"),
                        System.getenv("arm.password"),
                        System.getenv("arm.redirecturi"),
                        null)
        );
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        storageManagementClient = new StorageManagementClientImpl(
                new UserTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.username"),
                        System.getenv("arm.password"),
                        System.getenv("arm.redirecturi"),
                        null)
        );
        storageManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }
}
