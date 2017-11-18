/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;


import com.microsoft.azure.management.redis.implementation.RedisManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;

/**
 * The base for Redis cache manager tests.
 */
public class RedisManagementTest extends TestBase {
    protected static ResourceManager resourceManager;
    protected static RedisManager redisManager;
    protected static StorageManager storageManager;
    protected static String RG_NAME = "";
    protected static String RG_NAME_SECOND = "";
    protected static String RR_NAME = "";
    protected static String RR_NAME_SECOND = "";
    protected static String RR_NAME_THIRD = "";
    protected static String SA_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        RR_NAME = generateRandomResourceName("javacsmrc", 15);
        RG_NAME_SECOND = RG_NAME + "Second";
        RR_NAME_SECOND = RR_NAME + "Second";
        RR_NAME_THIRD = RR_NAME + "Third";
        SA_NAME = generateRandomResourceName("javacsmsa", 15);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        redisManager = RedisManager
                .authenticate(restClient, defaultSubscription);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
        resourceManager.resourceGroups().deleteByName(RG_NAME_SECOND);
    }
}

