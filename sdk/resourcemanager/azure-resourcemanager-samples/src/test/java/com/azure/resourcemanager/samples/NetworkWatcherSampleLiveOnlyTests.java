// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.network.samples.ManageNetworkWatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkWatcherSampleLiveOnlyTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testManageNetworkWatcher() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageNetworkWatcher.runSample(azureResourceManager));
    }
}
