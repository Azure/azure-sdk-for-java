// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubscriptionsTests extends ResourceManagementTest {
    protected ResourceManager.Authenticated resourceManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        resourceManager = ResourceManager
                .authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canListSubscriptions() throws Exception {
        PagedIterable<Subscription> subscriptions = resourceManager.subscriptions().list();
        Assertions.assertTrue(TestUtilities.getSize(subscriptions) > 0);
    }

    @Test
    public void canListLocations() throws Exception {
        PagedIterable<Location> locations = resourceManager.subscriptions().list().iterator().next().listLocations();
        Assertions.assertTrue(TestUtilities.getSize(locations) > 0);
    }
}
