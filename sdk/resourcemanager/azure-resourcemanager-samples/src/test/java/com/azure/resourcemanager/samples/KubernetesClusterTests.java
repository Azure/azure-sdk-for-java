// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.samples;


import com.azure.resourcemanager.kubernetescluster.samples.DeployImageFromContainerRegistryToKubernetes;
import com.azure.resourcemanager.kubernetescluster.samples.ManageKubernetesCluster;
import com.azure.resourcemanager.kubernetescluster.samples.ManagedKubernetesClusterWithAdvancedNetworking;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class KubernetesClusterTests extends SamplesTestBase {
    @Test
    @Disabled("QuotaExceeded error: Public preview limit of 5 for managed cluster(AKS) has been reached for subscription sub-id in location ukwest. Same error even after deleting all clusters")
    public void testManageKubernetesCluster() throws IOException, JSchException {
        if (isPlaybackMode()) {
            // Disable mocked testing but keep it commented out in case we want to re-enable it later
            // Assertions.assertTrue(ManageKubernetesCluster.runSample(azure, "client id", "secret"));
            return;
        } else {
            Assertions.assertTrue(ManageKubernetesCluster.runSample(azureResourceManager, "", ""));
        }
    }

    @Test
    @Disabled("QuotaExceeded error: Public preview limit of 5 for managed cluster(AKS) has been reached for subscription sub-id in location ukwest. Same error even after deleting all clusters")
    public void testManageKubernetesClusterWithAdvancedNetworking() throws IOException, JSchException {
        if (isPlaybackMode()) {
            // Disable mocked testing but keep it commented out in case we want to re-enable it later
            // Assertions.assertTrue(ManageKubernetesCluster.runSample(azure, "client id", "secret"));
            return;
        } else {
            Assertions.assertTrue(ManagedKubernetesClusterWithAdvancedNetworking.runSample(azureResourceManager, "", ""));
        }
    }

    @Test
    public void testDeployImageFromContainerRegistryToKubernetes() throws InterruptedException, JSchException, IOException {
        if (!isPlaybackMode()) {
            Assertions.assertTrue(DeployImageFromContainerRegistryToKubernetes.runSample(azureResourceManager, "", ""));
        }
    }
}
