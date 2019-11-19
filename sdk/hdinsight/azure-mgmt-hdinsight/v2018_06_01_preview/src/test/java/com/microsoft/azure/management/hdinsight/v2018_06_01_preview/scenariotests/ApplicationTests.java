/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.*;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ApplicationInner;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.Utilities.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTests extends HDInsightManagementTestBase {

    @Test
    public void testHueOnRunningCluster() {
        String clusterName = generateRandomClusterName("hdisdk-applications-hue");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().withClusterVersion("3.6");
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        String applicationName = "MyApplication";
        ApplicationInner application = new ApplicationInner().withProperties(new ApplicationProperties()
            .withInstallScriptActions(Collections.singletonList(
                new RuntimeScriptAction().withName("InstallHue")
                    .withUri("https://hdiconfigactions.blob.core.windows.net/linuxhueconfigactionv02/install-hue-uber-v02.sh")
                    .withParameters("-version latest -port 20000")
                    .withRoles(Collections.singletonList("edgenode"))
            ))
            .withApplicationType("CustomApplication")
            .withComputeProfile(new ComputeProfile()
                .withRoles(Collections.singletonList(
                    new Role().withName("edgenode")
                        .withHardwareProfile(new HardwareProfile()
                            .withVmSize("Large"))
                        .withTargetInstanceCount(1)
                ))
            )
        );

        hdInsightManager.applications().inner().create(resourceGroup.name(), clusterName, applicationName, application);
        List<ApplicationInner> applicationList = hdInsightManager.applications().inner().listByCluster(resourceGroup.name(), clusterName);
        assertThat(applicationList).isNotEmpty().hasSize(1);
        assertThat(applicationList.get(0).name()).isEqualToIgnoringCase(applicationName);
        hdInsightManager.applications().inner().delete(resourceGroup.name(), clusterName, applicationName);
        applicationList = hdInsightManager.applications().inner().listByCluster(resourceGroup.name(), clusterName);
        assertThat(applicationList).isEmpty();
    }
}
