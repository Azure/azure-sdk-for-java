// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.StorageManager;

/** The base for Redis cache manager tests. */
public class RedisManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected RedisManager redisManager;
    protected StorageManager storageManager;
    protected String RG_NAME = "";
    protected String RG_NAME_SECOND = "";
    protected String RR_NAME = "";
    protected String RR_NAME_SECOND = "";
    protected String RR_NAME_THIRD = "";
    protected String SA_NAME = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        RR_NAME = generateRandomResourceName("javacsmrc", 15);
        RG_NAME_SECOND = RG_NAME + "Second";
        RR_NAME_SECOND = RR_NAME + "Second";
        RR_NAME_THIRD = RR_NAME + "Third";
        SA_NAME = generateRandomResourceName("javacsmsa", 15);

        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        redisManager = RedisManager.authenticate(httpPipeline, profile, sdkContext);

        storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        try {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
        } catch (Exception e) {
        }
        try {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME_SECOND);
        } catch (Exception e) {
        }
    }
}
