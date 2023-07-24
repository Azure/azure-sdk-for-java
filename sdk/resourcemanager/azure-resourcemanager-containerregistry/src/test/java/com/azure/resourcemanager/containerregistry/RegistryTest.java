// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;

import java.time.temporal.ChronoUnit;
import java.util.List;

/** The base for storage manager tests. */
public abstract class RegistryTest extends ResourceManagerTestProxyTestBase {
    protected ResourceManager resourceManager;
    protected ContainerRegistryManager registryManager;
    protected String rgName;

    public RegistryTest() {
        addSanitizers(
            new TestProxySanitizer(String.format("$..%s", "uploadUrl"), null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer(String.format("$..%s", "logLink"), null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY)
        );
    }

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
        registryManager = buildManager(ContainerRegistryManager.class, httpPipeline, profile);
        resourceManager = registryManager.resourceManager();
        rgName = generateRandomResourceName("rgacr", 10);
    }
}
