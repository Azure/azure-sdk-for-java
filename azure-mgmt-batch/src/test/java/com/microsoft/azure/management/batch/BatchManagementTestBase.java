package com.microsoft.azure.management.batch;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;

public abstract class BatchManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static BatchManager batchManager;

    public static void createClients() {
        // TODO - ans - Enabled code below after finding out why it does not work and remove authfile usage.
        /*
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);
        */
        try {
            ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(new File("D:/my.azureauth.txt"));
            RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                    .withCredentials(credentials)
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                    .build();

            resourceManager = ResourceManager
                    .authenticate(restClient)
                    .withSubscription(System.getenv("subscription-id"));

            batchManager = BatchManager
                    .authenticate(restClient, System.getenv("subscription-id"));
        } catch (IOException ex) {

        }
    }
}
