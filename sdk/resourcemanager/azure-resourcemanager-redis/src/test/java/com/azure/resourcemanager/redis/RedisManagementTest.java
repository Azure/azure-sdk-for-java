// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;

/** The base for Redis cache manager tests. */
public class RedisManagementTest extends ResourceManagerTestBase {
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
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        rrName = generateRandomResourceName("javacsmrc", 15);
        rgNameSecond = rgName + "Second";
        rrNameSecond = rrName + "Second";
        rrNameThird = rrName + "Third";
        saName = generateRandomResourceName("javacsmsa", 15);

        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        redisManager = buildManager(RedisManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        resourceManager = redisManager.resourceManager();
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
