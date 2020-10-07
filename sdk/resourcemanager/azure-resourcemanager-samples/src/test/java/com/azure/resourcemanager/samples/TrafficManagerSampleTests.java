// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.trafficmanager.samples.ManageSimpleTrafficManager;
import com.azure.resourcemanager.trafficmanager.samples.ManageTrafficManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TrafficManagerSampleTests extends SamplesTestBase {
    @Test
    public void testManageSimpleTrafficManager() {
        Assertions.assertTrue(ManageSimpleTrafficManager.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageTrafficManager() throws IOException {
        if (this.skipInPlayback()) {
            // sample creates certificate
            return;
        }
        Assertions.assertTrue(ManageTrafficManager.runSample(azureResourceManager));
    }
}
