package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.RestClient;

public abstract class ComputeManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static ComputeManager computeManager;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                //.withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        computeManager = ComputeManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}
