/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterCreateParametersExtended;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Map;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.Utilities.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTests extends HDInsightManagementTestBase {

    @Test
    public void testGetConfigurations() {
        String clusterName = generateRandomClusterName("hdisdk-configs");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        String hiveSite = "hive-site";
        Map<String, String> hiveConfig = ImmutableMap.of(
            "key1", "value1",
            "key2", "value2"
        );

        String mapredSite = "mapred-site";
        Map<String, String> mapredConfig = ImmutableMap.of(
            "key5", "value5",
            "key6", "value6"
        );

        String yarnSite = "yarn-site";
        Map<String, String> yarnConfig = ImmutableMap.of(
            "key7", "value7",
            "key8", "value8"
        );

        String coreSite = "core-site";
        String gatewayName = "gateway";

        Map<String, Map<String, String>> configurations = (Map<String, Map<String, String>>) createParams
            .properties().clusterDefinition().configurations();
        configurations.put(hiveSite, hiveConfig);
        configurations.put(mapredSite, mapredConfig);
        configurations.put(yarnSite, yarnConfig);

        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        Map<String, String> hive = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, hiveSite);
        assertThat(hive).isEqualTo(hiveConfig);

        Map<String, String> mapred = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, mapredSite);
        assertThat(mapred).isEqualTo(mapredConfig);

        Map<String, String> yarn = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, yarnSite);
        assertThat(yarn).isEqualTo(yarnConfig);

        Map<String, String> gateway = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, gatewayName);
        assertThat(gateway).isNotEmpty().hasSize(3);

        Map<String, String> core = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, coreSite);
        assertThat(core).isNotEmpty().hasSize(2);
        assertThat(core).containsKey("fs.defaultFS");
        assertThat(core.keySet()).has(new Condition<Iterable<? extends String>>() {
            @Override
            public boolean matches(Iterable<? extends String> keys) {
                for (String key : keys) {
                    if (key.startsWith("fs.azure.account.key.")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Test
    public void testHttpExtended() {
        String clusterName = "hdisdk-http";
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        String gateway = "gateway";
        String expectedUserName = CLUSTER_USERNAME;
        String expectedUserPassword = CLUSTER_PASSWORD;
        Map<String, String> actualHttpSettings = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, gateway);
        validateHttpSettings(expectedUserName, expectedUserPassword, actualHttpSettings);

        String newExpectedUserPassword = "NewPassword1!";
        Map<String, String> updateParams = ImmutableMap.of(
            "restAuthCredential.isEnabled", "true",
            "restAuthCredential.username", expectedUserName,
            "restAuthCredential.password", newExpectedUserPassword
        );

        hdInsightManager.configurations().inner().update(resourceGroup.name(), clusterName, gateway, updateParams);
        actualHttpSettings = hdInsightManager.configurations().inner().get(resourceGroup.name(), clusterName, gateway);
        validateHttpSettings(expectedUserName, newExpectedUserPassword, actualHttpSettings);
    }
}
