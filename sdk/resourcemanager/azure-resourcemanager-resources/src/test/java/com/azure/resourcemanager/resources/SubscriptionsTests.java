// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class SubscriptionsTests extends ResourceManagerTestBase {
    protected ResourceManager.Authenticated resourceManager;

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
            null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        SdkContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        resourceManager = ResourceManager
                .authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canListSubscriptions() {
        PagedIterable<Subscription> subscriptions = resourceManager.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptions) > 0);
    }

    @Test
    public void canListLocations() {
        PagedIterable<Location> locations = resourceManager.subscriptions().list().iterator().next().listLocations();
        Assertions.assertTrue(TestUtilities.getSize(locations) > 0);
    }
}
