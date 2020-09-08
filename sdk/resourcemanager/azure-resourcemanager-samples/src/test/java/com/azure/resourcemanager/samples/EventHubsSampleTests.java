// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.eventhubs.samples.ManageEventHub;
import com.azure.resourcemanager.eventhubs.samples.ManageEventHubEvents;
import com.azure.resourcemanager.eventhubs.samples.ManageEventHubGeoDisasterRecovery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventHubsSampleTests extends SamplesTestBase {
    @Test
    public void testManageEventHub() throws Exception {
        Assertions.assertTrue(ManageEventHub.runSample(azure));
    }

    @Test
    public void testManageEventHubEvents() throws Exception {
        Assertions.assertTrue(ManageEventHubEvents.runSample(azure));
    }

    @Test
    public void testManageEventHubGeoDisasterRecovery() throws Exception {
        Assertions.assertTrue(ManageEventHubGeoDisasterRecovery.runSample(azure));
    }
}
