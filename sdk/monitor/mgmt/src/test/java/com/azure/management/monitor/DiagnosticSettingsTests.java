/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.storage.StorageAccount;
import org.junit.jupiter.api.Assertions;

public class DiagnosticSettingsTests extends MonitorManagementTest {
    private String RG_NAME = "";
    private String SA_NAME = "";
    private String DS_NAME="";
    private String EH_NAME="";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("jMonitor_", 18);
        SA_NAME = generateRandomResourceName("jMonitorSa", 18);
        DS_NAME = generateRandomResourceName("jMonitorDs_", 18);
        EH_NAME = generateRandomResourceName("jMonitorEH", 18);

        super.initializeClients(restClient, defaultSubscription, domain);
    }
    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
    }

    // FIXME: need eventhub service
//    @Test
//    public void canCRUDDiagnosticSettings() throws Exception {
//
//        VirtualMachine vm = computeManager.virtualMachines().list().iterator().next();
//
//        // clean all diagnostic settings.
//        PagedIterable<DiagnosticSetting> dsList = monitorManager.diagnosticSettings().listByResource(vm.id());
//        for(DiagnosticSetting dsd : dsList) {
//            monitorManager.diagnosticSettings().deleteById(dsd.id());
//        }
//
//        StorageAccount sa = storageManager.storageAccounts()
//                .define(SA_NAME)
//                // Storage Account should be in the same region as resource
//                .withRegion(vm.region())
//                .withNewResourceGroup(RG_NAME)
//                .withTag("tag1", "value1")
//                .create();
//
//        EventHubNamespace namespace = eventHubManager.namespaces()
//                .define(EH_NAME)
//                // EventHub should be in the same region as resource
//                .withRegion(vm.region())
//                .withNewResourceGroup(RG_NAME)
//                .withNewManageRule("mngRule1")
//                .withNewSendRule("sndRule1")
//                .create();
//
//        EventHubNamespaceAuthorizationRule evenHubNsRule = namespace.listAuthorizationRules().get(0);
//
//        List<DiagnosticSettingsCategory> categories = monitorManager.diagnosticSettings()
//                .listCategoriesByResource(vm.id());
//
//        Assertions.assertNotNull(categories);
//        Assertions.assertFalse(categories.isEmpty());
//
//        DiagnosticSetting setting = monitorManager.diagnosticSettings()
//                .define(DS_NAME)
//                .withResource(vm.id())
//                .withStorageAccount(sa.id())
//                .withEventHub(evenHubNsRule.id())
//                .withLogsAndMetrics(categories, Period.minutes(5), 7)
//                .create();
//
//        Assertions.assertTrue(vm.id().equalsIgnoreCase(setting.resourceId()));
//        Assertions.assertTrue(sa.id().equalsIgnoreCase(setting.storageAccountId()));
//        Assertions.assertTrue(evenHubNsRule.id().equalsIgnoreCase(setting.eventHubAuthorizationRuleId()));
//        Assertions.assertNull(setting.eventHubName());
//        Assertions.assertNull(setting.workspaceId());
//        Assertions.assertTrue(setting.logs().isEmpty());
//        Assertions.assertFalse(setting.metrics().isEmpty());
//
//        setting.update()
//                .withoutStorageAccount()
//                .withoutLogs()
//                .apply();
//
//        Assertions.assertTrue(vm.id().equalsIgnoreCase(setting.resourceId()));
//        Assertions.assertTrue(evenHubNsRule.id().equalsIgnoreCase(setting.eventHubAuthorizationRuleId()));
//        Assertions.assertNull(setting.storageAccountId());
//        Assertions.assertNull(setting.eventHubName());
//        Assertions.assertNull(setting.workspaceId());
//        Assertions.assertTrue(setting.logs().isEmpty());
//        Assertions.assertFalse(setting.metrics().isEmpty());
//
//        DiagnosticSetting ds1 = monitorManager.diagnosticSettings().get(setting.resourceId(), setting.name());
//        checkDiagnosticSettingValues(setting, ds1);
//
//        DiagnosticSetting ds2 = monitorManager.diagnosticSettings().getById(setting.id());
//        checkDiagnosticSettingValues(setting, ds2);
//
//        dsList = monitorManager.diagnosticSettings().listByResource(vm.id());
//        Assertions.assertNotNull(dsList);
//        Assertions.assertEquals(1, dsList.size());
//        DiagnosticSetting ds3 = dsList.get(0);
//        checkDiagnosticSettingValues(setting, ds3);
//
//        monitorManager.diagnosticSettings().deleteById(setting.id());
//
//        dsList = monitorManager.diagnosticSettings().listByResource(vm.id());
//        Assertions.assertNotNull(dsList);
//        Assertions.assertTrue(dsList.isEmpty());
//    }

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
            Assertions.assertTrue(expected.eventHubAuthorizationRuleId().equalsIgnoreCase(actual.eventHubAuthorizationRuleId()));
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

