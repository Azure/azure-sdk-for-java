// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.containerservice.samples.ManageKubernetesCluster;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerServiceSampleTests extends SamplesTestBase {

    @Test
    public void testManageKubernetesCluster() {
        Assertions.assertTrue(ManageKubernetesCluster.runSample(azureResourceManager));
    }
}
