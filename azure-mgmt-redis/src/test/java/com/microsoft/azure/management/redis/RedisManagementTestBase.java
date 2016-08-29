/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;


import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.redis.implementation.RedisManager;
import com.microsoft.azure.RestClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The base for storage manager tests.
 */
public abstract class RedisManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static RedisManager redisManager;

    protected static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        redisManager = RedisManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}

