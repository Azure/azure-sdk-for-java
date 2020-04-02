/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.samples;

import com.azure.management.network.samples.ManageNetworkWatcher;
import com.azure.management.resources.core.TestBase;
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
