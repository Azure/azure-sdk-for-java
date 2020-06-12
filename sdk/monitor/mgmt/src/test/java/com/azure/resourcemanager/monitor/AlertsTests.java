// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.ActivityLogAlert;
import com.azure.resourcemanager.monitor.models.DynamicThresholdFailingPeriods;
import com.azure.resourcemanager.monitor.models.DynamicThresholdOperator;
import com.azure.resourcemanager.monitor.models.DynamicThresholdSensitivity;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleTimeAggregation;
import com.azure.resourcemanager.monitor.models.MetricDimension;
import com.azure.resourcemanager.monitor.models.MetricDynamicAlertCondition;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlertsTests extends MonitorManagementTest {
    private String rgName = "";
    private String saName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("jMonitor_", 18);
        saName = generateRandomResourceName("jMonitorSA", 18);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDMetricAlerts() throws Exception {

        try {
            StorageAccount sa =
                storageManager
                    .storageAccounts()
                    .define(saName)
                    .withRegion(Region.US_EAST2)
                    .withNewResourceGroup(rgName)
                    .withOnlyHttpsTraffic()
                    .create();

            ActionGroup ag =
                monitorManager
                    .actionGroups()
                    .define("simpleActionGroup")
                    .withExistingResourceGroup(rgName)
                    .defineReceiver("first")
                    .withPushNotification("azurepush@outlook.com")
                    .withEmail("justemail@outlook.com")
                    .withSms("1", "4255655665")
                    .withVoice("1", "2062066050")
                    .withWebhook("https://www.rate.am")
                    .attach()
                    .defineReceiver("second")
                    .withEmail("secondemail@outlook.com")
                    .withWebhook("https://www.spyur.am")
                    .attach()
                    .create();

            MetricAlert ma =
                monitorManager
                    .alertRules()
                    .metricAlerts()
                    .define("somename")
                    .withExistingResourceGroup(rgName)
                    .withTargetResource(sa.id())
                    .withPeriod(Duration.ofMinutes(15))
                    .withFrequency(Duration.ofMinutes(1))
                    .withAlertDetails(
                        3,
                        "This alert rule is for U3 - Single resource  multiple-criteria  with dimensions-single"
                            + " timeseries")
                    .withActionGroups(ag.id())
                    .defineAlertCriteria("Metric1")
                    .withMetricName("Transactions", "Microsoft.Storage/storageAccounts")
                    .withCondition(MetricAlertRuleTimeAggregation.TOTAL, MetricAlertRuleCondition.GREATER_THAN, 100)
                    .withDimension("ResponseType", "Success")
                    .withDimension("ApiName", "GetBlob")
                    .attach()
                    .create();

            Assertions.assertNotNull(ma);
            Assertions.assertEquals(1, ma.scopes().size());
            Assertions.assertEquals(sa.id(), ma.scopes().iterator().next());
            Assertions
                .assertEquals(
                    "This alert rule is for U3 - Single resource  multiple-criteria  with dimensions-single timeseries",
                    ma.description());
            Assertions.assertEquals(Duration.ofMinutes(15), ma.windowSize());
            Assertions.assertEquals(Duration.ofMinutes(1), ma.evaluationFrequency());
            Assertions.assertEquals(3, ma.severity());
            Assertions.assertEquals(true, ma.enabled());
            Assertions.assertEquals(true, ma.autoMitigate());
            Assertions.assertEquals(1, ma.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), ma.actionGroupIds().iterator().next());
            Assertions.assertEquals(1, ma.alertCriterias().size());
            MetricAlertCondition ac1 = ma.alertCriterias().values().iterator().next();
            Assertions.assertEquals("Metric1", ac1.name());
            Assertions.assertEquals("Transactions", ac1.metricName());
            Assertions.assertEquals("Microsoft.Storage/storageAccounts", ac1.metricNamespace());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac1.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.TOTAL, ac1.timeAggregation());
            Assertions.assertEquals(100, ac1.threshold(), 0.001);
            Assertions.assertEquals(2, ac1.dimensions().size());
            Iterator<MetricDimension> iterator = ac1.dimensions().iterator();
            MetricDimension d2 = iterator.next();
            MetricDimension d1 = iterator.next();
            Assertions.assertEquals("ResponseType", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("Success", d1.values().get(0));
            Assertions.assertEquals("ApiName", d2.name());
            Assertions.assertEquals(1, d2.values().size());
            Assertions.assertEquals("GetBlob", d2.values().get(0));

            MetricAlert maFromGet = monitorManager.alertRules().metricAlerts().getById(ma.id());
            Assertions.assertNotNull(maFromGet);
            Assertions.assertEquals(ma.scopes().size(), maFromGet.scopes().size());
            Assertions.assertEquals(ma.scopes().iterator().next(), maFromGet.scopes().iterator().next());
            Assertions.assertEquals(ma.description(), maFromGet.description());
            Assertions.assertEquals(ma.windowSize(), maFromGet.windowSize());
            Assertions.assertEquals(ma.evaluationFrequency(), maFromGet.evaluationFrequency());
            Assertions.assertEquals(ma.severity(), maFromGet.severity());
            Assertions.assertEquals(ma.enabled(), maFromGet.enabled());
            Assertions.assertEquals(ma.autoMitigate(), maFromGet.autoMitigate());
            Assertions.assertEquals(ma.actionGroupIds().size(), maFromGet.actionGroupIds().size());
            Assertions
                .assertEquals(ma.actionGroupIds().iterator().next(), maFromGet.actionGroupIds().iterator().next());
            Assertions.assertEquals(ma.alertCriterias().size(), maFromGet.alertCriterias().size());
            ac1 = maFromGet.alertCriterias().values().iterator().next();
            Assertions.assertEquals("Metric1", ac1.name());
            Assertions.assertEquals("Transactions", ac1.metricName());
            Assertions.assertEquals("Microsoft.Storage/storageAccounts", ac1.metricNamespace());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac1.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.TOTAL, ac1.timeAggregation());
            Assertions.assertEquals(100, ac1.threshold(), 0.001);
            Assertions.assertEquals(2, ac1.dimensions().size());
            iterator = ac1.dimensions().iterator();
            d2 = iterator.next();
            d1 = iterator.next();
            Assertions.assertEquals("ResponseType", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("Success", d1.values().get(0));
            Assertions.assertEquals("ApiName", d2.name());
            Assertions.assertEquals(1, d2.values().size());
            Assertions.assertEquals("GetBlob", d2.values().get(0));

            ma
                .update()
                .withRuleDisabled()
                .updateAlertCriteria("Metric1")
                .withCondition(MetricAlertRuleTimeAggregation.TOTAL, MetricAlertRuleCondition.GREATER_THAN, 99)
                .parent()
                .defineAlertCriteria("Metric2")
                .withMetricName("SuccessE2ELatency", "Microsoft.Storage/storageAccounts")
                .withCondition(MetricAlertRuleTimeAggregation.AVERAGE, MetricAlertRuleCondition.GREATER_THAN, 200)
                .withDimension("ApiName", "GetBlob")
                .attach()
                .apply();

            Assertions.assertNotNull(ma);
            Assertions.assertEquals(1, ma.scopes().size());
            Assertions.assertEquals(sa.id(), ma.scopes().iterator().next());
            Assertions
                .assertEquals(
                    "This alert rule is for U3 - Single resource  multiple-criteria  with dimensions-single timeseries",
                    ma.description());
            Assertions.assertEquals(Duration.ofMinutes(15), ma.windowSize());
            Assertions.assertEquals(Duration.ofMinutes(1), ma.evaluationFrequency());
            Assertions.assertEquals(3, ma.severity());
            Assertions.assertEquals(false, ma.enabled());
            Assertions.assertEquals(true, ma.autoMitigate());
            Assertions.assertEquals(1, ma.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), ma.actionGroupIds().iterator().next());
            Assertions.assertEquals(2, ma.alertCriterias().size());
            Iterator<MetricAlertCondition> maCriteriaIterator = ma.alertCriterias().values().iterator();
            ac1 = maCriteriaIterator.next();
            MetricAlertCondition ac2 = maCriteriaIterator.next();
            Assertions.assertEquals("Metric1", ac1.name());
            Assertions.assertEquals("Transactions", ac1.metricName());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac1.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.TOTAL, ac1.timeAggregation());
            Assertions.assertEquals(99, ac1.threshold(), 0.001);
            Assertions.assertEquals(2, ac1.dimensions().size());
            iterator = ac1.dimensions().iterator();
            d2 = iterator.next();
            d1 = iterator.next();
            Assertions.assertEquals("ResponseType", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("Success", d1.values().get(0));
            Assertions.assertEquals("ApiName", d2.name());
            Assertions.assertEquals(1, d2.values().size());
            Assertions.assertEquals("GetBlob", d2.values().get(0));

            Assertions.assertEquals("Metric2", ac2.name());
            Assertions.assertEquals("SuccessE2ELatency", ac2.metricName());
            Assertions.assertEquals("Microsoft.Storage/storageAccounts", ac2.metricNamespace());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac2.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.AVERAGE, ac2.timeAggregation());
            Assertions.assertEquals(200, ac2.threshold(), 0.001);
            Assertions.assertEquals(1, ac2.dimensions().size());
            d1 = ac2.dimensions().iterator().next();
            Assertions.assertEquals("ApiName", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("GetBlob", d1.values().get(0));

            maFromGet = monitorManager.alertRules().metricAlerts().getById(ma.id());

            Assertions.assertNotNull(maFromGet);
            Assertions.assertEquals(1, maFromGet.scopes().size());
            Assertions.assertEquals(sa.id(), maFromGet.scopes().iterator().next());
            Assertions
                .assertEquals(
                    "This alert rule is for U3 - Single resource  multiple-criteria  with dimensions-single timeseries",
                    ma.description());
            Assertions.assertEquals(Duration.ofMinutes(15), maFromGet.windowSize());
            Assertions.assertEquals(Duration.ofMinutes(1), maFromGet.evaluationFrequency());
            Assertions.assertEquals(3, maFromGet.severity());
            Assertions.assertEquals(false, maFromGet.enabled());
            Assertions.assertEquals(true, maFromGet.autoMitigate());
            Assertions.assertEquals(1, maFromGet.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), maFromGet.actionGroupIds().iterator().next());
            Assertions.assertEquals(2, maFromGet.alertCriterias().size());
            maCriteriaIterator = maFromGet.alertCriterias().values().iterator();
            ac1 = maCriteriaIterator.next();
            ac2 = maCriteriaIterator.next();
            Assertions.assertEquals("Metric1", ac1.name());
            Assertions.assertEquals("Transactions", ac1.metricName());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac1.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.TOTAL, ac1.timeAggregation());
            Assertions.assertEquals(99, ac1.threshold(), 0.001);
            Assertions.assertEquals(2, ac1.dimensions().size());
            iterator = ac1.dimensions().iterator();
            d2 = iterator.next();
            d1 = iterator.next();
            Assertions.assertEquals("ResponseType", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("Success", d1.values().get(0));
            Assertions.assertEquals("ApiName", d2.name());
            Assertions.assertEquals(1, d2.values().size());
            Assertions.assertEquals("GetBlob", d2.values().get(0));

            Assertions.assertEquals("Metric2", ac2.name());
            Assertions.assertEquals("SuccessE2ELatency", ac2.metricName());
            Assertions.assertEquals("Microsoft.Storage/storageAccounts", ac2.metricNamespace());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac2.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.AVERAGE, ac2.timeAggregation());
            Assertions.assertEquals(200, ac2.threshold(), 0.001);
            Assertions.assertEquals(1, ac2.dimensions().size());
            d1 = ac2.dimensions().iterator().next();
            Assertions.assertEquals("ApiName", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("GetBlob", d1.values().get(0));

            PagedIterable<MetricAlert> alertsInRg =
                monitorManager.alertRules().metricAlerts().listByResourceGroup(rgName);

            Assertions.assertEquals(1, TestUtilities.getSize(alertsInRg));
            maFromGet = alertsInRg.iterator().next();

            Assertions.assertNotNull(maFromGet);
            Assertions.assertEquals(1, maFromGet.scopes().size());
            Assertions.assertEquals(sa.id(), maFromGet.scopes().iterator().next());
            Assertions
                .assertEquals(
                    "This alert rule is for U3 - Single resource  multiple-criteria  with dimensions-single timeseries",
                    ma.description());
            Assertions.assertEquals(Duration.ofMinutes(15), maFromGet.windowSize());
            Assertions.assertEquals(Duration.ofMinutes(1), maFromGet.evaluationFrequency());
            Assertions.assertEquals(3, maFromGet.severity());
            Assertions.assertEquals(false, maFromGet.enabled());
            Assertions.assertEquals(true, maFromGet.autoMitigate());
            Assertions.assertEquals(1, maFromGet.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), maFromGet.actionGroupIds().iterator().next());
            Assertions.assertEquals(2, maFromGet.alertCriterias().size());
            maCriteriaIterator = maFromGet.alertCriterias().values().iterator();
            ac1 = maCriteriaIterator.next();
            ac2 = maCriteriaIterator.next();
            Assertions.assertEquals("Metric1", ac1.name());
            Assertions.assertEquals("Transactions", ac1.metricName());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac1.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.TOTAL, ac1.timeAggregation());
            Assertions.assertEquals(99, ac1.threshold(), 0.001);
            Assertions.assertEquals(2, ac1.dimensions().size());
            iterator = ac1.dimensions().iterator();
            d2 = iterator.next();
            d1 = iterator.next();
            Assertions.assertEquals("ResponseType", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("Success", d1.values().get(0));
            Assertions.assertEquals("ApiName", d2.name());
            Assertions.assertEquals(1, d2.values().size());
            Assertions.assertEquals("GetBlob", d2.values().get(0));

            Assertions.assertEquals("Metric2", ac2.name());
            Assertions.assertEquals("SuccessE2ELatency", ac2.metricName());
            Assertions.assertEquals("Microsoft.Storage/storageAccounts", ac2.metricNamespace());
            Assertions.assertEquals(MetricAlertRuleCondition.GREATER_THAN, ac2.condition());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.AVERAGE, ac2.timeAggregation());
            Assertions.assertEquals(200, ac2.threshold(), 0.001);
            Assertions.assertEquals(1, ac2.dimensions().size());
            d1 = ac2.dimensions().iterator().next();
            Assertions.assertEquals("ApiName", d1.name());
            Assertions.assertEquals(1, d1.values().size());
            Assertions.assertEquals("GetBlob", d1.values().get(0));

            monitorManager.alertRules().metricAlerts().deleteById(ma.id());
        } finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canCRUDMultipleResourceMetricAlerts() throws Exception {
        try {
            final String userName = "tirekicker";
            final String password = password();

            String alertName = generateRandomResourceName("jMonitorMA", 18);
            String vmName1 = generateRandomResourceName("jMonitorVM1", 18);
            String vmName2 = generateRandomResourceName("jMonitorVM2", 18);

            VirtualMachine vm1 =
                computeManager
                    .virtualMachines()
                    .define(vmName1)
                    .withRegion(Region.US_EAST2)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .create();

            VirtualMachine vm2 =
                computeManager
                    .virtualMachines()
                    .define(vmName2)
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .create();

            MetricAlert ma =
                monitorManager
                    .alertRules()
                    .metricAlerts()
                    .define(alertName)
                    .withExistingResourceGroup(rgName)
                    .withMultipleTargetResources(Arrays.asList(vm1, vm2))
                    .withPeriod(Duration.ofMinutes(15))
                    .withFrequency(Duration.ofMinutes(5))
                    .withAlertDetails(3, "This alert rule is for U3 - Multiple resource, static criteria")
                    .withActionGroups()
                    .defineAlertCriteria("Metric1")
                    .withMetricName("Percentage CPU", vm1.type())
                    .withCondition(MetricAlertRuleTimeAggregation.AVERAGE, MetricAlertRuleCondition.GREATER_THAN, 80)
                    .attach()
                    .create();

            ma.refresh();
            Assertions.assertEquals(2, ma.scopes().size());
            Assertions.assertEquals(vm1.type(), ma.inner().targetResourceType());
            Assertions.assertEquals(vm1.regionName(), ma.inner().targetResourceRegion());
            Assertions.assertEquals(1, ma.alertCriterias().size());
            Assertions.assertEquals(0, ma.dynamicAlertCriterias().size());
            Assertions.assertEquals("Percentage CPU", ma.alertCriterias().get("Metric1").metricName());

            OffsetDateTime time30MinBefore = OffsetDateTime.now().minusMinutes(30);
            ma
                .update()
                .withDescription("This alert rule is for U3 - Multiple resource, dynamic criteria")
                .withoutAlertCriteria("Metric1")
                .defineDynamicAlertCriteria("Metric2")
                .withMetricName("Percentage CPU", vm1.type())
                .withCondition(
                    MetricAlertRuleTimeAggregation.AVERAGE,
                    DynamicThresholdOperator.GREATER_THAN,
                    DynamicThresholdSensitivity.HIGH)
                .withFailingPeriods(
                    new DynamicThresholdFailingPeriods()
                        .withNumberOfEvaluationPeriods(4)
                        .withMinFailingPeriodsToAlert(2))
                .withIgnoreDataBefore(time30MinBefore)
                .attach()
                .apply();

            ma.refresh();
            Assertions.assertEquals(2, ma.scopes().size());
            Assertions.assertEquals(vm1.type(), ma.inner().targetResourceType());
            Assertions.assertEquals(vm1.regionName(), ma.inner().targetResourceRegion());
            Assertions.assertEquals(0, ma.alertCriterias().size());
            Assertions.assertEquals(1, ma.dynamicAlertCriterias().size());
            MetricDynamicAlertCondition condition = ma.dynamicAlertCriterias().get("Metric2");
            Assertions.assertEquals("Percentage CPU", condition.metricName());
            Assertions.assertEquals(MetricAlertRuleTimeAggregation.AVERAGE, condition.timeAggregation());
            Assertions.assertEquals(DynamicThresholdOperator.GREATER_THAN, condition.condition());
            Assertions.assertEquals(DynamicThresholdSensitivity.HIGH, condition.alertSensitivity());
            Assertions.assertEquals(4, (int) condition.failingPeriods().numberOfEvaluationPeriods());
            Assertions.assertEquals(2, (int) condition.failingPeriods().minFailingPeriodsToAlert());
            Assertions.assertEquals(time30MinBefore, condition.ignoreDataBefore());

            monitorManager.alertRules().metricAlerts().deleteById(ma.id());
        } finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canCRUDActivityLogAlerts() throws Exception {

        try {
            ActionGroup ag =
                monitorManager
                    .actionGroups()
                    .define("simpleActionGroup")
                    .withNewResourceGroup(rgName, Region.US_EAST2)
                    .defineReceiver("first")
                    .withPushNotification("azurepush@outlook.com")
                    .withEmail("justemail@outlook.com")
                    .withSms("1", "4255655665")
                    .withVoice("1", "2062066050")
                    .withWebhook("https://www.rate.am")
                    .attach()
                    .defineReceiver("second")
                    .withEmail("secondemail@outlook.com")
                    .withWebhook("https://www.spyur.am")
                    .attach()
                    .create();

            VirtualMachine justAvm = computeManager.virtualMachines().list().iterator().next();

            ActivityLogAlert ala =
                monitorManager
                    .alertRules()
                    .activityLogAlerts()
                    .define("somename")
                    .withExistingResourceGroup(rgName)
                    .withTargetSubscription(monitorManager.subscriptionId())
                    .withDescription("AutoScale-VM-Creation-Failed")
                    .withRuleEnabled()
                    .withActionGroups(ag.id())
                    .withEqualsCondition("category", "Administrative")
                    .withEqualsCondition("resourceId", justAvm.id())
                    .withEqualsCondition("operationName", "Microsoft.Compute/virtualMachines/delete")
                    .create();

            Assertions.assertNotNull(ala);
            Assertions.assertEquals(1, ala.scopes().size());
            Assertions
                .assertEquals("/subscriptions/" + monitorManager.subscriptionId(), ala.scopes().iterator().next());
            Assertions.assertEquals("AutoScale-VM-Creation-Failed", ala.description());
            Assertions.assertEquals(true, ala.enabled());
            Assertions.assertEquals(1, ala.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), ala.actionGroupIds().iterator().next());
            Assertions.assertEquals(3, ala.equalsConditions().size());
            Assertions.assertEquals("Administrative", ala.equalsConditions().get("category"));
            Assertions.assertEquals(justAvm.id(), ala.equalsConditions().get("resourceId"));
            Assertions
                .assertEquals("Microsoft.Compute/virtualMachines/delete", ala.equalsConditions().get("operationName"));

            ActivityLogAlert alaFromGet = monitorManager.alertRules().activityLogAlerts().getById(ala.id());

            Assertions.assertEquals(ala.scopes().size(), alaFromGet.scopes().size());
            Assertions.assertEquals(ala.scopes().iterator().next(), alaFromGet.scopes().iterator().next());
            Assertions.assertEquals(ala.description(), alaFromGet.description());
            Assertions.assertEquals(ala.enabled(), alaFromGet.enabled());
            Assertions.assertEquals(ala.actionGroupIds().size(), alaFromGet.actionGroupIds().size());
            Assertions
                .assertEquals(ala.actionGroupIds().iterator().next(), alaFromGet.actionGroupIds().iterator().next());
            Assertions.assertEquals(ala.equalsConditions().size(), alaFromGet.equalsConditions().size());
            Assertions
                .assertEquals(ala.equalsConditions().get("category"), alaFromGet.equalsConditions().get("category"));
            Assertions
                .assertEquals(
                    ala.equalsConditions().get("resourceId"), alaFromGet.equalsConditions().get("resourceId"));
            Assertions
                .assertEquals(
                    ala.equalsConditions().get("operationName"), alaFromGet.equalsConditions().get("operationName"));

            ala
                .update()
                .withRuleDisabled()
                .withoutEqualsCondition("operationName")
                .withEqualsCondition("status", "Failed")
                .apply();

            Assertions.assertEquals(1, ala.scopes().size());
            Assertions
                .assertEquals("/subscriptions/" + monitorManager.subscriptionId(), ala.scopes().iterator().next());
            Assertions.assertEquals("AutoScale-VM-Creation-Failed", ala.description());
            Assertions.assertEquals(false, ala.enabled());
            Assertions.assertEquals(1, ala.actionGroupIds().size());
            Assertions.assertEquals(ag.id(), ala.actionGroupIds().iterator().next());
            Assertions.assertEquals(3, ala.equalsConditions().size());
            Assertions.assertEquals("Administrative", ala.equalsConditions().get("category"));
            Assertions.assertEquals(justAvm.id(), ala.equalsConditions().get("resourceId"));
            Assertions.assertEquals("Failed", ala.equalsConditions().get("status"));
            Assertions.assertEquals(false, ala.equalsConditions().containsKey("operationName"));

            PagedIterable<ActivityLogAlert> alertsInRg =
                monitorManager.alertRules().activityLogAlerts().listByResourceGroup(rgName);

            Assertions.assertEquals(1, TestUtilities.getSize(alertsInRg));
            alaFromGet = alertsInRg.iterator().next();

            Assertions.assertEquals(ala.scopes().size(), alaFromGet.scopes().size());
            Assertions.assertEquals(ala.scopes().iterator().next(), alaFromGet.scopes().iterator().next());
            Assertions.assertEquals(ala.description(), alaFromGet.description());
            Assertions.assertEquals(ala.enabled(), alaFromGet.enabled());
            Assertions.assertEquals(ala.actionGroupIds().size(), alaFromGet.actionGroupIds().size());
            Assertions
                .assertEquals(ala.actionGroupIds().iterator().next(), alaFromGet.actionGroupIds().iterator().next());
            Assertions.assertEquals(ala.equalsConditions().size(), alaFromGet.equalsConditions().size());
            Assertions
                .assertEquals(ala.equalsConditions().get("category"), alaFromGet.equalsConditions().get("category"));
            Assertions
                .assertEquals(
                    ala.equalsConditions().get("resourceId"), alaFromGet.equalsConditions().get("resourceId"));
            Assertions.assertEquals(ala.equalsConditions().get("status"), alaFromGet.equalsConditions().get("status"));
            Assertions
                .assertEquals(
                    ala.equalsConditions().containsKey("operationName"),
                    alaFromGet.equalsConditions().containsKey("operationName"));

            monitorManager.alertRules().activityLogAlerts().deleteById(ala.id());
        } finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }
}
