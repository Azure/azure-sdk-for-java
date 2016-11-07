/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.website.implementation.AppServiceManager;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * The base for storage manager tests.
 */
public abstract class AppServiceTestBase {
    protected static ResourceManager resourceManager;
    protected static AppServiceManager appServiceManager;

    protected static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String s) {
                        System.out.println(s);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
                .withReadTimeout(1, TimeUnit.MINUTES)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        appServiceManager = AppServiceManager
                .authenticate(restClient, System.getenv("domain"), System.getenv("subscription-id"));
    }
}