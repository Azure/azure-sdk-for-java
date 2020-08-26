// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.eventhubs.samples.ManageEventHub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventHubsSampleLiveOnlyTests extends SamplesTestBase {
    public EventHubsSampleLiveOnlyTests() {
        super(RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageEventHub() {
        Assertions.assertTrue(ManageEventHub.runSample(azure));
    }
}
