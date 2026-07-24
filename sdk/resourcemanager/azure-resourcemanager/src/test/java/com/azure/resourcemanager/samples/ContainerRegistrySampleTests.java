// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerRegistrySampleTests extends SamplesTestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageContainerRegistry() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ManageContainerRegistry.runSample(azureResourceManager));
    }
}
