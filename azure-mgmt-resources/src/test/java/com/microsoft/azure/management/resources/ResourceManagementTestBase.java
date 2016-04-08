package com.microsoft.azure.management.resources;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.AzureResource;
import com.microsoft.azure.management.resources.models.Subscription;

public abstract class ResourceManagementTestBase {
    protected AzureResourceAuthenticated resourceClient;
    protected Subscription subscription;

    public ResourceManagementTestBase() throws Exception {
        resourceClient = AzureResource.authenticate(
                new ApplicationTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.secret"),
                        null)
        );
        subscription = resourceClient.subscriptions().get(System.getenv("arm.subscriptionid"));
    }
}
