package com.microsoft.azure.management.resources;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class ResourceManagerTestBase {
    protected static ResourceManager resourceClient;

    public static void createClient() throws Exception {
        resourceClient = ResourceManager
                .configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .authenticate(
                new ApplicationTokenCredentials(
                        System.getenv("arm.clientid"),
                        System.getenv("arm.domain"),
                        System.getenv("arm.secret"),
                        null)
        ).withSubscription(System.getenv("arm.subscriptionid"));
    }
}
