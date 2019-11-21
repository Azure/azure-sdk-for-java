/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterCreateParametersExtended;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterMonitoringRequest;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterMonitoringResponseInner;
import org.junit.Test;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.Utilities.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionTests extends HDInsightManagementTestBase {

    @Test
    public void testOmsOnRunningCluster() {
        String clusterName = generateRandomClusterName("hdisdk-oms");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().clusterDefinition().withKind("Spark");
        createParams.properties().withClusterVersion("3.6");
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        hdInsightManager.extensions().inner().enableMonitoring(
            resourceGroup.name(),
            clusterName,
            new ClusterMonitoringRequest().withWorkspaceId(WORKSPACE_ID)
        );

        ClusterMonitoringResponseInner monitoringStatus = hdInsightManager.extensions().inner()
            .getMonitoringStatus(resourceGroup.name(), clusterName);
        assertThat(monitoringStatus.clusterMonitoringEnabled()).isTrue();
        assertThat(monitoringStatus.workspaceId()).isEqualTo(WORKSPACE_ID);

        hdInsightManager.extensions().inner().disableMonitoring(resourceGroup.name(), clusterName);
        monitoringStatus = hdInsightManager.extensions().inner()
            .getMonitoringStatus(resourceGroup.name(), clusterName);
        assertThat(monitoringStatus.clusterMonitoringEnabled()).isFalse();
        assertThat(monitoringStatus.workspaceId()).isNull();
    }
}
