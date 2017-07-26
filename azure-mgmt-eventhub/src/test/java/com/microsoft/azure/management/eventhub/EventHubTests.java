/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class EventHubTests extends TestBase {
    protected EventHubManager eventHubManager;
    protected ResourceManager resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        eventHubManager = EventHubManager.authenticate(restClient, defaultSubscription);
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() { }

    @Test
    public void canAuthenticateEventHubManager() {
        Assert.assertNotNull(eventHubManager);
    }
}