package com.microsoft.azure.management.sql;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;
import okhttp3.logging.HttpLoggingInterceptor;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public abstract class SqlServerTestBase {
    protected static ResourceManager resourceManager;
    protected static SqlServerManager sqlServerManager;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
//                .withProxy( new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .withReadTimeout(100, TimeUnit.SECONDS)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        sqlServerManager = SqlServerManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}
