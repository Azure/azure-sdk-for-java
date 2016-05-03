package com.microsoft.azure.management.resources;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

public abstract class ResourceManagerTestBase {
    protected static ResourceManager.Authenticated resourceClient;
    protected static ResourceManager.Subscription subscription;

    public static void createClient() throws Exception {
        resourceClient = ResourceManager.authenticate(
                new ApplicationTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.secret"),
                        null)
        );
        subscription = resourceClient.withSubscription(System.getenv("arm.subscriptionid"));
    }
}
