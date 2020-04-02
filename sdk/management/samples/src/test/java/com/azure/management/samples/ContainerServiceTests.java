/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.containerservice.samples.DeployImageFromContainerRegistryToContainerServiceOrchestrator;
import com.azure.management.containerservice.samples.ManageContainerServiceWithDockerSwarmOrchestrator;
import com.azure.management.containerservice.samples.ManageContainerServiceWithKubernetesOrchestrator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class ContainerServiceTests extends SamplesTestBase {
    @Test
    public void testManageContainerServiceWithKubernetesOrchestrator() {
        if (isPlaybackMode()) {
            // Disable mocked testing but keep it commented out in case we want to re-enable it later
            // Assertions.assertTrue(ManageContainerServiceWithKubernetesOrchestrator.runSample(azure, "client id", "secret"));
        } else {
            Assertions.assertTrue(ManageContainerServiceWithKubernetesOrchestrator.runSample(azure, "", ""));
        }
    }

    @Test
    @Disabled("Container service will be deprecated")
    public void testManageContainerServiceWithDockerSwarmOrchestrator() {
        Assertions.assertTrue(ManageContainerServiceWithDockerSwarmOrchestrator.runSample(azure));
    }

    @Test
    public void testDeployImageFromContainerRegistryToContainerServiceOrchestrator() {
        if (!isPlaybackMode()) {
            Assertions.assertTrue(DeployImageFromContainerRegistryToContainerServiceOrchestrator.runSample(azure, "", ""));
        }
    }

}
