// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class SqlServerTest extends ResourceManagerTestBase {
    protected ResourceManager resourceManager;
    protected SqlServerManager sqlServerManager;
    protected StorageManager storageManager;
    protected String rgName = "";
    protected String sqlServerName = "";

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
        rgName = generateRandomResourceName("rgsql", 20);
        sqlServerName = generateRandomResourceName("javasqlserver", 20);
        SdkContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        SdkContext sdkContext = new SdkContext();
        sdkContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        sqlServerManager = SqlServerManager.authenticate(httpPipeline, profile, sdkContext);
        storageManager = StorageManager.authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        SdkContext.sleep(1000);
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
