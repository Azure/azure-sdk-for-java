package com.microsoft.azure.management.resources;

import com.microsoft.rest.credentials.UserTokenCredentials;

public abstract class ResourceManagementTestBase {
    protected ResourceManagementClient resourceManagementClient;

    public ResourceManagementTestBase() {
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
    }
}
