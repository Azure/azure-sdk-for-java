/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.samples;


import com.azure.management.kubernetescluster.samples.DeployImageFromContainerRegistryToKubernetes;
import com.azure.management.kubernetescluster.samples.ManageKubernetesCluster;
import com.azure.management.kubernetescluster.samples.ManagedKubernetesClusterWithAdvancedNetworking;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class KubernetesClusterTests extends SamplesTestBase {
    @Test
    @Disabled("QuotaExceeded error: Public preview limit of 5 for managed cluster(AKS) has been reached for subscription sub-id in location ukwest. Same error even after deleting all clusters")
    public void testManageKubernetesCluster() {
        if (isPlaybackMode()) {
            // Disable mocked testing but keep it commented out in case we want to re-enable it later
            // Assertions.assertTrue(ManageKubernetesCluster.runSample(azure, "client id", "secret"));
        } else {
            Assertions.assertTrue(ManageKubernetesCluster.runSample(azure, "", ""));
        }
    }

    @Test
    @Disabled("QuotaExceeded error: Public preview limit of 5 for managed cluster(AKS) has been reached for subscription sub-id in location ukwest. Same error even after deleting all clusters")
    public void testManageKubernetesClusterWithAdvancedNetworking() {
        if (isPlaybackMode()) {
            // Disable mocked testing but keep it commented out in case we want to re-enable it later
            // Assertions.assertTrue(ManageKubernetesCluster.runSample(azure, "client id", "secret"));
        } else {
            Assertions.assertTrue(ManagedKubernetesClusterWithAdvancedNetworking.runSample(azure, "", ""));
        }
    }

    @Test
    public void testDeployImageFromContainerRegistryToKubernetes() {
        if (!isPlaybackMode()) {
            Assertions.assertTrue(DeployImageFromContainerRegistryToKubernetes.runSample(azure, "", ""));
        }
    }
}
