// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hdinsight.containers.generated;

import com.azure.resourcemanager.hdinsight.containers.models.ClusterAksPatchVersionUpgradeProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterHotfixUpgradeProperties;
import com.azure.resourcemanager.hdinsight.containers.models.ClusterUpgrade;

/**
 * Samples for Clusters Upgrade.
 */
public final class ClustersUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeAKSPatchVersionForCluster.json
     */
    /**
     * Sample code: ClustersUpgradeAKSPatchVersion.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void clustersUpgradeAKSPatchVersion(
        com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .upgrade("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterUpgrade().withProperties(new ClusterAksPatchVersionUpgradeProperties()),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/HDInsightOnAks/preview/2024-05-01-preview/examples/
     * UpgradeHotfixForCluster.json
     */
    /**
     * Sample code: ClustersUpgradeHotfix.
     * 
     * @param manager Entry point to HDInsightContainersManager.
     */
    public static void
        clustersUpgradeHotfix(com.azure.resourcemanager.hdinsight.containers.HDInsightContainersManager manager) {
        manager.clusters()
            .upgrade("hiloResourcegroup", "clusterpool1", "cluster1",
                new ClusterUpgrade().withProperties(new ClusterHotfixUpgradeProperties().withTargetOssVersion("1.16.0")
                    .withTargetClusterVersion("1.0.6")
                    .withTargetBuildNumber("3")
                    .withComponentName("historyserver")),
                com.azure.core.util.Context.NONE);
    }
}
