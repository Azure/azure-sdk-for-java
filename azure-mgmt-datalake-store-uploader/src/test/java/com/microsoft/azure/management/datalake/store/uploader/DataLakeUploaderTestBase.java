/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreFileSystemManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.azure.RestClient;
import com.microsoft.rest.serializer.JacksonMapperAdapter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import java.util.concurrent.TimeUnit;

public abstract class DataLakeUploaderTestBase {
    protected static ResourceManagementClientImpl resourceManagementClient;
    protected static DataLakeStoreAccountManagementClientImpl dataLakeStoreAccountManagementClient;
    protected static DataLakeStoreFileSystemManagementClientImpl dataLakeStoreFileSystemManagementClient;

    public static void createClients() {
        UserTokenCredentials credentials = new UserTokenCredentials(
                System.getenv("arm.clientid"),
                System.getenv("arm.domain"),
                System.getenv("arm.username"),
                System.getenv("arm.password"),
                null,
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withDefaultBaseUrl(AzureEnvironment.AZURE)
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();

        resourceManagementClient = new ResourceManagementClientImpl(restClient);
        resourceManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));

        dataLakeStoreAccountManagementClient = new DataLakeStoreAccountManagementClientImpl(restClient);
        dataLakeStoreAccountManagementClient.withSubscriptionId(System.getenv("arm.subscriptionid"));

        RestClient dataPlaneClient = new RestClient.Builder(new OkHttpClient.Builder().connectTimeout(100, TimeUnit.SECONDS), new Retrofit.Builder())
                .withBaseUrl("https://{accountName}.{adlsFileSystemDnsSuffix}")
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.NONE) // No logging for this client because we are executing a lot of requests.
                .build();

        dataLakeStoreFileSystemManagementClient = new DataLakeStoreFileSystemManagementClientImpl(dataPlaneClient);
    }

    public static String generateName(String prefix) {
        int randomSuffix = (int) (Math.random() * 1000);
        return prefix + randomSuffix;
    }
}
