// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.monitor.fluent.models.DiagnosticSettingsResourceInner;
import com.azure.resourcemanager.monitor.models.DiagnosticSetting;
import com.azure.resourcemanager.monitor.models.DiagnosticSettingsCategory;
import com.azure.resourcemanager.monitor.models.LogSettings;
import com.azure.resourcemanager.monitor.models.MetricSettings;
import com.azure.resourcemanager.monitor.models.RetentionPolicy;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.Sku;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
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
    public void canCRUDDiagnosticSettings() {

        // make sure there exists a VM
        Region region = Region.US_WEST;
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        String vmName = generateRandomResourceName("jMonitorVm_", 18);
        VirtualMachine vm = ensureVM(region, resourceGroup, vmName, "10.0.0.0/28");

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

        assertResourceIdEquals(vm.id(), setting.resourceId());
        assertResourceIdEquals(sa.id(), setting.storageAccountId());
        assertResourceIdEquals(evenHubNsRule.id(), setting.eventHubAuthorizationRuleId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertTrue(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        setting.update()
                .withoutStorageAccount()
                .withoutLogs()
                .apply();

        assertResourceIdEquals(vm.id(), setting.resourceId());
        assertResourceIdEquals(evenHubNsRule.id(), setting.eventHubAuthorizationRuleId());
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

    @Test
    public void canCRUDDiagnosticSettingsForSubscription() {
        Region region = Region.US_WEST;
        StorageAccount sa = storageManager.storageAccounts()
            .define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withTag("tag1", "value1")
            .create();

        String resourceId = "subscriptions/" + monitorManager.subscriptionId();

        DiagnosticSetting setting = monitorManager.diagnosticSettings()
            .define(dsName)
            .withResource(resourceId)
            .withStorageAccount(sa.id())
            .withLog("Security", 7)
            .create();

        try {
            if (!isPlaybackMode()) {
                Assertions.assertTrue(resourceId.equalsIgnoreCase(setting.resourceId()));
            }
            assertResourceIdEquals(sa.id(), setting.storageAccountId());

            Assertions.assertFalse(setting.logs().isEmpty());
            Assertions.assertTrue(setting.metrics().isEmpty());

            DiagnosticSetting ds1 = monitorManager.diagnosticSettings().get(setting.resourceId(), setting.name());
            checkDiagnosticSettingValues(setting, ds1);

            DiagnosticSetting ds2 = monitorManager.diagnosticSettings().getById(setting.id());
            checkDiagnosticSettingValues(setting, ds2);

            // removing all metrics and logs from a diagnostic setting, is equivalent to deleting the setting itself
            setting.update()
                .withoutLog("Security")
                .apply();

            // "get" will throw 404 since the setting is deleted
            Assertions.assertThrows(ManagementException.class, setting::refresh);

            Assertions.assertFalse(
                monitorManager.diagnosticSettings()
                    .listByResource(resourceId)
                    .stream()
                    .anyMatch(s -> s.name().equals(dsName)));

            setting.update()
                .withLog("Security", 7)
                .apply();

            setting.refresh();

            Assertions.assertTrue(
                monitorManager.diagnosticSettings()
                    .listByResource(resourceId)
                    .stream()
                    .anyMatch(s -> s.name().equals(dsName)));
        } finally {
            monitorManager.diagnosticSettings().deleteById(setting.id());
        }
    }

    @Test
    public void canCRUDDiagnosticSettingsForVault() {

        Region region = Region.US_WEST;

        StorageAccount sa = storageManager.storageAccounts()
                .define(saName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withTag("tag1", "value1")
                .create();

        Vault vault = ensureVault(region, rgName);

        // clean all diagnostic settings.
        List<DiagnosticSetting> dsList = monitorManager.diagnosticSettings().listByResource(vault.id()).stream().collect(Collectors.toList());
        for (DiagnosticSetting dsd : dsList) {
            monitorManager.diagnosticSettings().deleteById(dsd.id());
        }

        List<DiagnosticSettingsCategory> categories = monitorManager.diagnosticSettings()
                .listCategoriesByResource(vault.id());

        Assertions.assertNotNull(categories);
        Assertions.assertFalse(categories.isEmpty());

        DiagnosticSetting setting = monitorManager.diagnosticSettings()
                .define(dsName)
                .withResource(vault.id())
                .withStorageAccount(sa.id())
                .withLogsAndMetrics(categories, Duration.ofMinutes(5), 7)
                .create();

        Assertions.assertTrue(vault.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertNotNull(setting.storageAccountId());
        Assertions.assertNull(setting.eventHubAuthorizationRuleId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertFalse(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        setting.update()
                .withoutLogs()
                .apply();

        Assertions.assertTrue(vault.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertNotNull(setting.storageAccountId());
        Assertions.assertNull(setting.eventHubAuthorizationRuleId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertTrue(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        DiagnosticSetting ds1 = monitorManager.diagnosticSettings().get(setting.resourceId(), setting.name());
        checkDiagnosticSettingValues(setting, ds1);

        DiagnosticSetting ds2 = monitorManager.diagnosticSettings().getById(setting.id());
        checkDiagnosticSettingValues(setting, ds2);

        dsList = monitorManager.diagnosticSettings().listByResource(vault.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertEquals(1, dsList.size());
        DiagnosticSetting ds3 = dsList.get(0);
        checkDiagnosticSettingValues(setting, ds3);

        DiagnosticSettingsResourceInner inner = setting.innerModel();
        inner.withLogs(new ArrayList<>())
            .logs().add(new LogSettings().withEnabled(true).withCategoryGroup("audit"));
        monitorManager.serviceClient().getDiagnosticSettingsOperations().createOrUpdate(vault.id(), setting.name(), inner);

        setting.refresh();

        Assertions.assertTrue(setting.logs().stream().anyMatch(logSettings -> "audit".equals(logSettings.categoryGroup())));

        // verify category logs and category group logs can both be present during update
        // issue: https://github.com/Azure/azure-sdk-for-java/issues/35425
        // mixture of category group and category logs aren't supported
        Assertions.assertThrows(ManagementException.class,
            () -> setting.update()
                .withLog("AuditEvent", 7)
                .apply());

        setting.refresh();

        Assertions.assertTrue(setting.logs().stream().anyMatch(logSettings -> "audit".equals(logSettings.categoryGroup())));
        Assertions.assertTrue(setting.logs().stream().noneMatch(logSettings -> "AuditEvent".equals(logSettings.category())));
        Assertions.assertTrue(setting.logs().stream().allMatch(logSettings -> logSettings.category() == null));
        Assertions.assertFalse(setting.metrics().isEmpty());

        monitorManager.diagnosticSettings().deleteById(setting.id());

        dsList = monitorManager.diagnosticSettings().listByResource(vault.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertTrue(dsList.isEmpty());
    }

    @Test
    public void canCRUDDiagnosticSettingsLogsCategoryGroup() {
        Region region = Region.US_WEST;

        String wpsName = generateRandomResourceName("jMonitorWps", 18);

        // resource (webpubsub) to monitor
        GenericResource wpsResource = monitorManager.resourceManager().genericResources()
            .define(wpsName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withResourceType("WebPubSub")
            .withProviderNamespace("Microsoft.SignalRService")
            .withoutPlan()
            .withSku(new Sku().withName("Free_F1").withTier("Free").withSize("F1").withCapacity(1))
            .withApiVersion("2021-10-01")
            .create();

        // storage account to store diagnostic data
        StorageAccount sa = storageManager.storageAccounts()
            .define(saName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withTag("tag1", "value1")
            .create();

        // diagnostic setting
        DiagnosticSetting setting = monitorManager.diagnosticSettings()
            .define(dsName)
            .withResource(wpsResource.id())
            .withStorageAccount(sa.id())
            .withLog("MessagingLogs", 7)
            .create();

        // add category group "audit" to log settings
        DiagnosticSettingsResourceInner inner = setting.innerModel();
        inner.logs().clear();   // Remove category "MessagingLogs". Diagnostic setting does not support mix of log category and log category group.
        inner.logs().add(new LogSettings().withCategoryGroup("audit").withEnabled(true).withRetentionPolicy(new RetentionPolicy().withEnabled(false)));
        monitorManager.serviceClient().getDiagnosticSettingsOperations().createOrUpdate(wpsResource.id(), dsName, inner);

        // verify category group "audit"
        setting = monitorManager.diagnosticSettings().listByResource(wpsResource.id()).iterator().next();
        Assertions.assertTrue(setting.logs().stream().anyMatch(ls -> "audit".equals(ls.categoryGroup())));

        // update to add metric
        setting.update()
            .withMetric("AllMetrics", Duration.ofMinutes(5), 7)
            .apply();

        // verify category group "audit"
        setting = monitorManager.diagnosticSettings().listByResource(wpsResource.id()).iterator().next();
        Assertions.assertTrue(setting.logs().stream().anyMatch(ls -> "audit".equals(ls.categoryGroup())));
        // verify metric "AllMetrics"
        Assertions.assertTrue(setting.metrics().stream().anyMatch(ms -> "AllMetrics".equals(ms.category())));
    }

    @Test
    public void canCRUDDiagnosticSettingsWithResourceIdWhiteSpace() {
        Region region = Region.US_EAST;

        StorageAccount sa = storageManager.storageAccounts()
            .define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withTag("tag1", "value1")
            .create();

        SqlElasticPool sqlElasticPool = ensureElasticPoolWithWhiteSpace(region, rgName);

        // clean all diagnostic settings.
        List<DiagnosticSetting> dsList = monitorManager.diagnosticSettings().listByResource(sqlElasticPool.id()).stream().collect(Collectors.toList());
        for (DiagnosticSetting dsd : dsList) {
            monitorManager.diagnosticSettings().deleteById(dsd.id());
        }

        List<DiagnosticSettingsCategory> categories = monitorManager.diagnosticSettings()
            .listCategoriesByResource(sqlElasticPool.id());

        Assertions.assertNotNull(categories);
        Assertions.assertFalse(categories.isEmpty());

        DiagnosticSetting setting = monitorManager.diagnosticSettings()
            .define(dsName)
            .withResource(sqlElasticPool.id())
            .withStorageAccount(sa.id())
            .withLogsAndMetrics(categories, Duration.ofMinutes(5), 7)
            .create();

        Assertions.assertTrue(sqlElasticPool.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertNotNull(setting.storageAccountId());
        Assertions.assertNull(setting.eventHubAuthorizationRuleId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertFalse(setting.metrics().isEmpty());

        setting.update()
            .withoutMetric("InstanceAndAppAdvanced")
            .apply();

        Assertions.assertTrue(sqlElasticPool.id().equalsIgnoreCase(setting.resourceId()));
        Assertions.assertNotNull(setting.storageAccountId());
        Assertions.assertNull(setting.eventHubAuthorizationRuleId());
        Assertions.assertNull(setting.eventHubName());
        Assertions.assertNull(setting.workspaceId());
        Assertions.assertTrue(setting.logs().isEmpty());
        Assertions.assertFalse(setting.metrics().isEmpty());

        DiagnosticSetting ds1 = monitorManager.diagnosticSettings().get(setting.resourceId(), setting.name());
        checkDiagnosticSettingValues(setting, ds1);

        DiagnosticSetting ds2 = monitorManager.diagnosticSettings().getById(setting.id());
        checkDiagnosticSettingValues(setting, ds2);

        dsList = monitorManager.diagnosticSettings().listByResource(sqlElasticPool.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertEquals(1, dsList.size());
        DiagnosticSetting ds3 = dsList.get(0);
        checkDiagnosticSettingValues(setting, ds3);

        monitorManager.diagnosticSettings().deleteById(setting.id());

        dsList = monitorManager.diagnosticSettings().listByResource(sqlElasticPool.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertTrue(dsList.isEmpty());

        // test deleteByIds
        setting = monitorManager.diagnosticSettings()
            .define(dsName)
            .withResource(sqlElasticPool.id())
            .withStorageAccount(sa.id())
            .withLogsAndMetrics(categories, Duration.ofMinutes(5), 7)
            .create();

        dsList = monitorManager.diagnosticSettings().listByResource(sqlElasticPool.id()).stream().collect(Collectors.toList());
        Assertions.assertNotNull(dsList);
        Assertions.assertEquals(1, dsList.size());

        monitorManager.diagnosticSettings().deleteByIds(setting.id());

        dsList = monitorManager.diagnosticSettings().listByResource(sqlElasticPool.id()).stream().collect(Collectors.toList());
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
            Assertions.assertEquals(
                expected.logs().stream().filter(LogSettings::enabled).count(),
                actual.logs().stream().filter(LogSettings::enabled).count());
        }
        if (expected.metrics() == null) {
            Assertions.assertNull(actual.metrics());
        } else {
            Assertions.assertEquals(
                expected.metrics().stream().filter(MetricSettings::enabled).count(),
                actual.metrics().stream().filter(MetricSettings::enabled).count());
        }
    }
}
