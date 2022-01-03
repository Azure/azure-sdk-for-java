// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class DiagnosticSettingsTests extends MonitorManagementTest {
    private String rgName = "";
    private String saName = "";
    private String dsName = "";
    private String ehName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("jMonitor_", 18);
        saName = generateRandomResourceName("jMonitorSa", 18);
        dsName = generateRandomResourceName("jMonitorDs_", 18);
        ehName = generateRandomResourceName("jMonitorEH", 18);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDDiagnosticSettings() throws Exception {

        // make sure there exists a VM

        VirtualMachine vm = computeManager.virtualMachines().list().iterator().next();

        // clean all diagnostic settings.
        List<DiagnosticSetting> dsList = monitorManager.diagnosticSettings().listByResource(vm.id()).stream().collect(Collectors.toList());
        for (DiagnosticSetting dsd : dsList) {
            monitorManager.diagnosticSettings().deleteById(dsd.id());
        }

        StorageAccount sa = storageManager.storageAccounts()
                .define(saName)
                // Storage Account should be in the same region as resource
                .withRegion(vm.region())
                .withNewResourceGroup(rgName)
                .withTag("tag1", "value1")
                .create();

        EventHubNamespace namespace = eventHubManager.namespaces()
                .define(ehName)
                // EventHub should be in the same region as resource
                .withRegion(vm.region())
                .withNewResourceGroup(rgName)
                .withNewManageRule("mngRule1")
                .withNewSendRule("sndRule1")
                .create();

        EventHubNamespaceAuthorizationRule evenHubNsRule = namespace.listAuthorizationRules().iterator().next();

        List<DiagnosticSettingsCategory> categories = monitorManager.diagnosticSettings()
                .listCategoriesByResource(vm.id());

        Assertions.assertNotNull(categories);
        Assertions.assertFalse(categories.isEmpty());

        DiagnosticSetting setting = monitorManager.diagnosticSettings()
                .define(dsName)
                .withResource(vm.id())
                .withStorageAccount(sa.id())
                .withEventHub(evenHubNsRule.id())
                .withLogsAndMetrics(categories, Duration.ofMinutes(5), 7)
                .create();

        Assertions.assertTrue(vm.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertTrue(sa.id().equalsIgnoreCase(setting.storageAccountId()));
        Assertions.assertTrue(evenHubNsRule.id().equalsIgnoreCase(setting.eventHubAuthorizationRuleId()));
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertTrue(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        setting.update()
                .withoutStorageAccount()
                .withoutLogs()
                .apply();

        Assertions.assertTrue(vm.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertTrue(evenHubNsRule.id().equalsIgnoreCase(setting.eventHubAuthorizationRuleId()));
        Assertions.assertNull(setting.storageAccountId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertTrue(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        DiagnosticSetting ds1 = monitorManager.diagnosticSettings().get(setting.resourceId(), setting.name());
        checkDiagnosticSettingValues(setting, ds1);

        DiagnosticSetting ds2 = monitorManager.diagnosticSettings().getById(setting.id());
        checkDiagnosticSettingValues(setting, ds2);

        dsList = monitorManager.diagnosticSettings().listByResource(vm.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertEquals(1, dsList.size());
        DiagnosticSetting ds3 = dsList.get(0);
        checkDiagnosticSettingValues(setting, ds3);

        monitorManager.diagnosticSettings().deleteById(setting.id());

        dsList = monitorManager.diagnosticSettings().listByResource(vm.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertTrue(dsList.isEmpty());
    }

    private void checkDiagnosticSettingValues(DiagnosticSetting expected, DiagnosticSetting actual) {
        Assertions.assertTrue(expected.resourceId().equalsIgnoreCase(actual.resourceId()));
        Assertions.assertTrue(expected.name().equalsIgnoreCase(actual.name()));

        if (expected.workspaceId() == null) {
            Assertions.assertNull(actual.workspaceId());
        } else {
            Assertions.assertTrue(expected.workspaceId().equalsIgnoreCase(actual.workspaceId()));
        }
        if (expected.storageAccountId() == null) {
            Assertions.assertNull(actual.storageAccountId());
        } else {
            Assertions.assertTrue(expected.storageAccountId().equalsIgnoreCase(actual.storageAccountId()));
        }
        if (expected.eventHubAuthorizationRuleId() == null) {
            Assertions.assertNull(actual.eventHubAuthorizationRuleId());
        } else {
            Assertions
                .assertTrue(
                    expected.eventHubAuthorizationRuleId().equalsIgnoreCase(actual.eventHubAuthorizationRuleId()));
        }
        if (expected.eventHubName() == null) {
            Assertions.assertNull(actual.eventHubName());
        } else {
            Assertions.assertTrue(expected.eventHubName().equalsIgnoreCase(actual.eventHubName()));
        }
        // arrays
        if (expected.logs() == null) {
            Assertions.assertNull(actual.logs());
        } else {
            Assertions.assertEquals(expected.logs().size(), actual.logs().size());
        }
        if (expected.metrics() == null) {
            Assertions.assertNull(actual.metrics());
        } else {
            Assertions.assertEquals(expected.metrics().size(), actual.metrics().size());
        }
    }
}
