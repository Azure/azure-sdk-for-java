// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistry;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistryWithWebhooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ContainerRegistryTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testManageContainerRegistry() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageContainerRegistry.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageContainerRegistryWithWebhooks() throws IOException, InterruptedException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageContainerRegistryWithWebhooks.runSample(azureResourceManager));
    }
}
