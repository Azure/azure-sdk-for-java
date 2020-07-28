// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistry;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistryWithWebhooks;
import com.azure.resourcemanager.resources.core.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerRegistryTests extends SamplesTestBase {
    public ContainerRegistryTests() {
        // Failing in playback - dependent on Docker client/glassfish jersey library which is expecting a real connection
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageContainerRegistry() {
        Assertions.assertTrue(ManageContainerRegistry.runSample(azure));
    }

    @Test
    public void testManageContainerRegistryWithWebhooks() {
        Assertions.assertTrue(ManageContainerRegistryWithWebhooks.runSample(azure));
    }
}
