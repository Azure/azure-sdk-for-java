package com.microsoft.azure.management.resources;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ResourceManagementTestBase {
    protected ResourceManagementClient resourceManagementClient;

    public ResourceManagementTestBase() {
        resourceManagementClient = new ResourceManagementClientImpl(
                new ApplicationTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.secret"),
                        null)
        );
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
