// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.resourcemanager.base.profile.AzureProfile;
import com.azure.resourcemanager.resources.core.ResourceGroupTaggingPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.test.ResourceManagerTestBase;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * The base for resource manager tests.
 */
class ResourceManagementTest extends ResourceManagerTestBase {
    protected ResourceManager resourceClient;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceClient = ResourceManager
                .authenticate(httpPipeline, profile)
                .withSdkContext(sdkContext)
                .withDefaultSubscription();

    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile, List<HttpPipelinePolicy> policies) {
        if (isPlaybackMode()) {
            policies.add(new ResourceGroupTaggingPolicy());
            policies.add(new CookiePolicy());
            return HttpPipelineProvider.buildHttpPipeline(
                null, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
                null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, null);
        } else {
            policies.add(new ResourceGroupTaggingPolicy());
            policies.add(new TimeoutPolicy(Duration.ofMinutes(1)));
            policies.add(new CookiePolicy());
            return HttpPipelineProvider.buildHttpPipeline(
                credential, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
                null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, generateHttpClientWithProxy(null));
        }
    }

    @Override
    protected void cleanUpResources() {

    }
}
