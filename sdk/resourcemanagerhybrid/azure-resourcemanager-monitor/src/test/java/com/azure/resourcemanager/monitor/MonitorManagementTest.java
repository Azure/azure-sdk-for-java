// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;

/** The base for Monitor manager tests. */
public class MonitorManagementTest extends ResourceManagerTestBase {
    protected ResourceManager resourceManager;
    protected MonitorManager monitorManager;
    protected ComputeManager computeManager;
    protected StorageManager storageManager;
    protected EventHubsManager eventHubManager;
    protected AppServiceManager appServiceManager;

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
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        appServiceManager = buildManager(AppServiceManager.class, httpPipeline, profile);
        monitorManager = buildManager(MonitorManager.class, httpPipeline, profile);
        computeManager = buildManager(ComputeManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        eventHubManager = buildManager(EventHubsManager.class, httpPipeline, profile);
        resourceManager = monitorManager.resourceManager();
        setInternalContext(internalContext, computeManager);
    }

    @Override
    protected void cleanUpResources() {
    }
}
