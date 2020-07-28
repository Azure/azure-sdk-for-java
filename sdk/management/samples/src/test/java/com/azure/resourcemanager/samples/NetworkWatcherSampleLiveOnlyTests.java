// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.network.samples.ManageNetworkWatcher;
import com.azure.resourcemanager.resources.core.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkWatcherSampleLiveOnlyTests extends SamplesTestBase {
    public NetworkWatcherSampleLiveOnlyTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageNetworkWatcher() {
        Assertions.assertTrue(ManageNetworkWatcher.runSample(azure));
    }
}
