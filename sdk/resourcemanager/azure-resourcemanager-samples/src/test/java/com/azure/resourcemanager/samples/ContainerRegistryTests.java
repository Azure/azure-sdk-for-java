// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistry;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistryWithWebhooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerRegistryTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testManageContainerRegistry() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageContainerRegistry.runSample(azure));
    }

    @Test
    @DoNotRecord
    public void testManageContainerRegistryWithWebhooks() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageContainerRegistryWithWebhooks.runSample(azure));
    }
}
