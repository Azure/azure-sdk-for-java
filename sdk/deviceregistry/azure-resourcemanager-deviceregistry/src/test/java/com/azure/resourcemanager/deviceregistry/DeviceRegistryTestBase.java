// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Base class for Device Registry live tests.
 */
public abstract class DeviceRegistryTestBase extends ResourceManagerTestProxyTestBase {
    protected DeviceRegistryManager deviceRegistryManager;

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        deviceRegistryManager = DeviceRegistryManager.authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        // Subclasses override if cleanup is needed
    }
}
