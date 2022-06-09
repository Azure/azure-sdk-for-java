// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.kubernetescluster.samples.DeployImageFromContainerRegistryToKubernetes;
import com.azure.resourcemanager.kubernetescluster.samples.ManageKubernetesCluster;
import com.azure.resourcemanager.kubernetescluster.samples.ManagedKubernetesClusterWithAdvancedNetworking;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class KubernetesClusterTests extends SamplesTestBase {
    @Test
    public void testManageKubernetesCluster() {
        Assertions.assertTrue(ManageKubernetesCluster.runSample(azureResourceManager));
    }

    @Test
    public void testManageKubernetesClusterWithAdvancedNetworking() {
        Assertions.assertTrue(ManagedKubernetesClusterWithAdvancedNetworking.runSample(azureResourceManager));
    }

    @Test
    public void testDeployImageFromContainerRegistryToKubernetes() throws JSchException, IOException, InterruptedException {
        if (!isPlaybackMode()) {
            Assertions.assertTrue(DeployImageFromContainerRegistryToKubernetes.runSample(azureResourceManager, "", ""));
        }
    }
}
