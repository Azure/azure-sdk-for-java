package com.microsoft.azure.management.datalake.store;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;


import org.junit.Assert;

import java.util.UUID;

import okhttp3.logging.HttpLoggingInterceptor;

public abstract class DataLakeStoreManagementTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static DataLakeStoreAccountManagementClient dataLakeStoreAccountManagementClient;

    public static void createClients() {
        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                null,
                AzureEnvironment.AZURE);

        resourceManagementClient = new ResourceManagementClientImpl(credentials);
        resourceManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
        resourceManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(credentials);
        dataLakeStoreAccountManagementClient.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        dataLakeStoreAccountManagementClient.setSubscriptionId(System.getenv("arm.subscriptionid"));
    }
}