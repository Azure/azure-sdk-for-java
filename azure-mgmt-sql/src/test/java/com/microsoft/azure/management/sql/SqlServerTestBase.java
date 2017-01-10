package com.microsoft.azure.management.sql;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class SqlServerTestBase {
    protected static ResourceManager resourceManager;
    protected static SqlServerManager sqlServerManager;

    public static void createClients() throws IOException {
        final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
        ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
//                .withProxy( new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .withReadTimeout(100, TimeUnit.SECONDS)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(credentials.defaultSubscriptionId());

        sqlServerManager = SqlServerManager
                .authenticate(restClient, credentials.defaultSubscriptionId());
    }
}
