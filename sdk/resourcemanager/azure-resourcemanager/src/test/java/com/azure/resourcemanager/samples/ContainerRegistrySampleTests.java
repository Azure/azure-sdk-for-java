// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.containerregistry.samples.ManageContainerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerRegistrySampleTests extends SamplesTestBase {

    // Recorded on a personal subscription because the shared test subscription lacks
    // Microsoft.Authorization/roleAssignments/write, which the sample's AcrPull grant requires.
    @Test
    public void testManageContainerRegistry() {
        Assertions.assertTrue(ManageContainerRegistry.runSample(azureResourceManager));
    }
}
