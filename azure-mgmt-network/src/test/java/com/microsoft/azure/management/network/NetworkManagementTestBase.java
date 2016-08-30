package com.microsoft.azure.management.network;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.RestClient;

public abstract class NetworkManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static NetworkManager networkManager;

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
                .withSubscription(System.getenv("arm.subscription-id"));

        networkManager = NetworkManager
                .authenticate(restClient, System.getenv("arm.subscription-id"));
    }
}