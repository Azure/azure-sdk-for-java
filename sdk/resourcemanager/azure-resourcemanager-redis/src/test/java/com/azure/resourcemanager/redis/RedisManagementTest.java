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
    protected String rgName = "";
    protected String rgNameSecond = "";
    protected String rrName = "";
    protected String rrNameSecond = "";
    protected String rrNameThird = "";
    protected String saName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        rrName = generateRandomResourceName("javacsmrc", 15);
        rgNameSecond = rgName + "Second";
        rrNameSecond = rrName + "Second";
        rrNameThird = rrName + "Third";
        saName = generateRandomResourceName("javacsmsa", 15);

        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        redisManager = RedisManager.authenticate(httpPipeline, profile, sdkContext);

        storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        try {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
        try {
            resourceManager.resourceGroups().beginDeleteByName(rgNameSecond);
        } catch (Exception e) {
        }
    }
}
