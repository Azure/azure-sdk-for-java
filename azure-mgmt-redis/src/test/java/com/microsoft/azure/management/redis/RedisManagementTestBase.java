/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;


import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.redis.implementation.RedisManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The base for Redis cache manager tests.
 */
public abstract class RedisManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static RedisManager redisManager;
    protected static StorageManager storageManager;

    protected static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                // uncomment for Fiddler tracing
                //.withProxy( new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        redisManager = RedisManager
                .authenticate(restClient, System.getenv("subscription-id"));

        storageManager = StorageManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}

