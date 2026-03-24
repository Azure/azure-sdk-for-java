// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for Discovery management live tests.
 * 
 * Uses EUAP endpoint (eastus2euap.management.azure.com) since the 2026-02-01-preview
 * API is only available there, not in production management.azure.com.
 */
public class DiscoveryManagementTest extends ResourceManagerTestProxyTestBase {
    protected ResourceManager resourceManager;
    protected DiscoveryManager discoveryManager;
    protected String rgName = "";

    // EUAP endpoint for Discovery API 2026-02-01-preview
    private static final String EUAP_RESOURCE_MANAGER_ENDPOINT = "https://eastus2euap.management.azure.com/";

    // Custom Azure environment pointing to EUAP
    private static final AzureEnvironment AZURE_EUAP;

    static {
        // Create a custom AzureEnvironment that uses EUAP endpoint for Resource Manager
        // Copy all endpoints from AZURE and override resourceManagerEndpointUrl
        Map<String, String> endpoints = new HashMap<>(AzureEnvironment.AZURE.getEndpoints());
        endpoints.put("resourceManagerEndpointUrl", EUAP_RESOURCE_MANAGER_ENDPOINT);
        AZURE_EUAP = new AzureEnvironment(endpoints);
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javadiscoveryrg", 20);

        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));

        // Create EUAP profile for Discovery manager (only in non-playback mode)
        AzureProfile euapProfile;
        if (isPlaybackMode()) {
            euapProfile = profile;  // Use playback profile as-is
        } else {
            // Use EUAP environment for LIVE and RECORD modes
            Configuration configuration = Configuration.getGlobalConfiguration();
            String tenantId = Objects.requireNonNull(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID),
                "'AZURE_TENANT_ID' environment variable cannot be null.");
            String subscriptionId
                = Objects.requireNonNull(configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID),
                    "'AZURE_SUBSCRIPTION_ID' environment variable cannot be null.");
            euapProfile = new AzureProfile(tenantId, subscriptionId, AZURE_EUAP);
        }

        // Build the Discovery manager using EUAP profile
        discoveryManager = DiscoveryManager.authenticate(httpPipeline, euapProfile);
        resourceManager = buildManager(ResourceManager.class, httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        // Clean up any resources created during tests if needed
    }
}
