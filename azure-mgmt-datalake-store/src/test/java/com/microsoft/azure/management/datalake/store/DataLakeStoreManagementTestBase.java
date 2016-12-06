package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.RestClient;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class DataLakeStoreManagementTestBase {
    protected static ResourceManagementClientImpl resourceManagementClient;
    protected static DataLakeStoreAccountManagementClientImpl dataLakeStoreAccountManagementClient;

    public static void createClients() {
        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl("https://management.azure.com")
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();

        resourceManagementClient = new ResourceManagementClientImpl(restClient);
        resourceManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(restClient);
        dataLakeStoreAccountManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));
    }

    public static String generateName(String prefix) {
        int randomSuffix = (int) (Math.random() * 1000);
        return prefix + randomSuffix;
    }
}