package com.microsoft.azure.management.resources;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.AzureResourceManager;

public abstract class ResourceManagementTestBase {
    protected AzureResourceManager.Authenticated resourceClient;
    protected AzureResourceManager.Subscription subscription;

    public ResourceManagementTestBase() throws Exception {
        resourceClient = AzureResourceManager.authenticate(
                new ApplicationTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.secret"),
                        null)
        );
        subscription = resourceClient.withSubscription(System.getenv("arm.subscriptionid"));
    }
}
