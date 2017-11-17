/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class SubscriptionsTests extends TestBase {
    protected static ResourceManager.Authenticated resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canListSubscriptions() throws Exception {
        PagedList<Subscription> subscriptions = resourceManager.subscriptions().list();
        Assert.assertTrue(subscriptions.size() > 0);
    }

    @Test
    public void canListLocations() throws Exception {
        PagedList<Location> locations = resourceManager.subscriptions().list().get(0).listLocations();
        Assert.assertTrue(locations.size() > 0);
    }
}
