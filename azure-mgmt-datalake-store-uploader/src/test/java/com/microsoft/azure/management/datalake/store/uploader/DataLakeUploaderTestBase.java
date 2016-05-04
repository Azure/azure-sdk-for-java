/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.datalake.store.DataLakeStoreAccountManagementClient;
import com.microsoft.azure.management.datalake.store.DataLakeStoreAccountManagementClientImpl;
import com.microsoft.azure.management.datalake.store.DataLakeStoreFileSystemManagementClient;
import com.microsoft.azure.management.datalake.store.DataLakeStoreFileSystemManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import okhttp3.logging.HttpLoggingInterceptor;

public abstract class DataLakeUploaderTestBase {
    protected static ResourceManagementClient resourceManagementClient;
    protected static DataLakeStoreAccountManagementClient dataLakeStoreAccountManagementClient;
    protected static DataLakeStoreFileSystemManagementClient dataLakeStoreFileSystemManagementClient;

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

        dataLakeStoreFileSystemManagementClient = new DataLakeStoreFileSystemManagementClientImpl(credentials);
        dataLakeStoreFileSystemManagementClient.setLogLevel(HttpLoggingInterceptor.Level.NONE);
    }

    public static String generateName(String prefix) {
        int randomSuffix = (int)(Math.random() * 1000);
        return prefix + randomSuffix;
    }
}
