/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ClusterCreateParametersExtended;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.ExecuteScriptActionParameters;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.RuntimeScriptAction;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.RuntimeScriptActionDetailInner;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.Utilities.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ScriptActionTests extends HDInsightManagementTestBase {

    @Test
    public void testScriptActionsOnRunningCluster() {
        String clusterName = generateRandomClusterName("hdisdk-scriptactions");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        String installGiraph = "https://hdiconfigactions.blob.core.windows.net/linuxgiraphconfigactionv01/giraph-installer-v01.sh";
        String scriptName = "script1";

        // Execute script actions, and persist on success.
        ExecuteScriptActionParameters scriptActionParams = getExecuteScriptActionParams(scriptName, installGiraph, true);
        hdInsightManager.clusters().inner().executeScriptActions(resourceGroup.name(), clusterName, scriptActionParams);

        // List script actions and validate script is persisted.
        PagedList<RuntimeScriptActionDetailInner> scriptActionList = hdInsightManager.scriptActions().inner().listByCluster(resourceGroup.name(), clusterName);
        assertThat(scriptActionList).hasSize(1);
        RuntimeScriptActionDetailInner scriptAction = scriptActionList.get(0);
        assertThat(scriptAction.name()).isEqualTo(scriptActionParams.scriptActions().get(0).name());
        assertThat(scriptAction.uri()).isEqualTo(scriptActionParams.scriptActions().get(0).uri());
        assertThat(scriptAction.roles()).isEqualTo(scriptActionParams.scriptActions().get(0).roles());

        // Delete script action.
        hdInsightManager.scriptActions().inner().delete(resourceGroup.name(), clusterName, scriptName);

        // List script actions and validate script is deleted.
        scriptActionList = hdInsightManager.scriptActions().inner().listByCluster(resourceGroup.name(), clusterName);
        assertThat(scriptActionList).isEmpty();

        // List script action history and validate script appears there.
        PagedList<RuntimeScriptActionDetailInner> listHistoryResponse = hdInsightManager.scriptExecutionHistorys().inner().listByCluster(
            resourceGroup.name(),
            clusterName);
        assertThat(listHistoryResponse).hasSize(1);
        scriptAction = listHistoryResponse.get(0);
        assertThat(scriptAction.executionSummary()).hasSize(1);
        assertThat(scriptAction.name()).isEqualTo(scriptActionParams.scriptActions().get(0).name());
        assertThat(scriptAction.uri()).isEqualTo(scriptActionParams.scriptActions().get(0).uri());
        assertThat(scriptAction.roles()).isEqualTo(scriptActionParams.scriptActions().get(0).roles());
        assertThat(scriptAction.status()).isEqualTo("Succeeded");

        // Get the script action by ID and validate it's the same action.
        scriptAction = hdInsightManager.scriptActions().inner().getExecutionDetail(
            resourceGroup.name(),
            clusterName,
            scriptAction.scriptExecutionId().toString());
        assertThat(scriptAction.name()).isEqualTo(scriptActionParams.scriptActions().get(0).name());

        // Execute script actions, but don't persist on success.
        scriptActionParams = getExecuteScriptActionParams("script5baf", installGiraph, false);
        hdInsightManager.clusters().inner().executeScriptActions(resourceGroup.name(), clusterName, scriptActionParams);

        // List script action history and validate the new script also appears.
        listHistoryResponse = hdInsightManager.scriptExecutionHistorys().inner()
            .listByCluster(resourceGroup.name(), clusterName);
        assertThat(listHistoryResponse).hasSize(2);
        final String expectedScriptActionName = scriptActionParams.scriptActions().get(0).name();
        scriptAction = Iterables.find(listHistoryResponse, new Predicate<RuntimeScriptActionDetailInner>() {
            @Override
            public boolean apply(RuntimeScriptActionDetailInner scriptAction) {
                return scriptAction.name().equals(expectedScriptActionName);
            }
        });
        assertThat(scriptAction.executionSummary()).hasSize(1);
        assertThat(scriptAction.name()).isEqualTo(scriptActionParams.scriptActions().get(0).name());
        assertThat(scriptAction.uri()).isEqualTo(scriptActionParams.scriptActions().get(0).uri());
        assertThat(scriptAction.roles()).isEqualTo(scriptActionParams.scriptActions().get(0).roles());
        assertThat(scriptAction.status()).isEqualTo("Succeeded");

        // Promote non-persisted script.
        hdInsightManager.scriptExecutionHistorys().inner().promote(
            resourceGroup.name(),
            clusterName,
            listHistoryResponse.get(0).scriptExecutionId().toString()
        );

        // List script action list and validate the promoted script is the only one there.
        scriptActionList = hdInsightManager.scriptActions().inner().listByCluster(resourceGroup.name(), clusterName);
        assertThat(scriptActionList).hasSize(1);
        scriptAction = scriptActionList.get(0);
        assertThat(scriptAction.name()).isEqualTo(scriptActionParams.scriptActions().get(0).name());
        assertThat(scriptAction.uri()).isEqualTo(scriptActionParams.scriptActions().get(0).uri());
        assertThat(scriptAction.roles()).isEqualTo(scriptActionParams.scriptActions().get(0).roles());

        // List script action history and validate all three scripts are there.
        listHistoryResponse = hdInsightManager.scriptExecutionHistorys().inner().listByCluster(
            resourceGroup.name(),
            clusterName);
        assertThat(listHistoryResponse).hasSize(2);
        assertThat(listHistoryResponse).doesNotHave(new Condition<List<? extends RuntimeScriptActionDetailInner>>() {
            @Override
            public boolean matches(List<? extends RuntimeScriptActionDetailInner> scriptActions) {
                for (RuntimeScriptActionDetailInner scriptAction : scriptActions) {
                    if (!scriptAction.status().equals("Succeeded")) {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private ExecuteScriptActionParameters getExecuteScriptActionParams(String scriptName, String scriptUri, boolean persistOnSuccess) {
        return new ExecuteScriptActionParameters()
            .withPersistOnSuccess(persistOnSuccess)
            .withScriptActions(Collections.singletonList(
                new RuntimeScriptAction()
                    .withName(scriptName)
                    .withUri(scriptUri)
                    .withRoles(Arrays.asList("headnode", "workernode"))
            ));
    }
}
