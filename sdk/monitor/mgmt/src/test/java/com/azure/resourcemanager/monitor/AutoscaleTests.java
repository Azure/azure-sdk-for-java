// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.monitor.models.AutoscaleProfile;
import com.azure.resourcemanager.monitor.models.AutoscaleSetting;
import com.azure.resourcemanager.monitor.models.ComparisonOperationType;
import com.azure.resourcemanager.monitor.models.DayOfWeek;
import com.azure.resourcemanager.monitor.models.MetricStatisticType;
import com.azure.resourcemanager.monitor.models.RecurrenceFrequency;
import com.azure.resourcemanager.monitor.models.ScaleDirection;
import com.azure.resourcemanager.monitor.models.ScaleRule;
import com.azure.resourcemanager.monitor.models.ScaleType;
import com.azure.resourcemanager.monitor.models.TimeAggregationType;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;

public class AutoscaleTests extends MonitorManagementTest {
    private static String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("jMonitor_", 18);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDAutoscale() throws Exception {

        try {
            resourceManager
                .resourceGroups()
                .define(rgName)
                .withRegion(Region.US_EAST2)
                .withTag("type", "autoscale")
                .withTag("tagname", "tagvalue")
                .create();

            AppServicePlan servicePlan =
                appServiceManager
                    .appServicePlans()
                    .define("HighlyAvailableWebApps")
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(rgName)
                    .withPricingTier(PricingTier.PREMIUM_P1)
                    .withOperatingSystem(OperatingSystem.WINDOWS)
                    .create();

            AutoscaleSetting setting =
                monitorManager
                    .autoscaleSettings()
                    .define("somesettingZ")
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(rgName)
                    .withTargetResource(servicePlan.id())
                    .defineAutoscaleProfile("Default")
                    .withScheduleBasedScale(3)
                    .withRecurrentSchedule("UTC", "18:00", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.SATURDAY)
                    .attach()
                    .defineAutoscaleProfile("AutoScaleProfile1")
                    .withMetricBasedScale(1, 10, 1)
                    .defineScaleRule()
                    .withMetricSource(servicePlan.id())
                    // current swagger does not support namespace selection
                    // .withMetricName("CPUPercentage", "Microsoft.Web/serverfarms")
                    .withMetricName("CPUPercentage")
                    .withStatistic(Duration.ofMinutes(10), Duration.ofMinutes(1), MetricStatisticType.AVERAGE)
                    .withCondition(TimeAggregationType.AVERAGE, ComparisonOperationType.GREATER_THAN, 70)
                    .withScaleAction(ScaleDirection.INCREASE, ScaleType.EXACT_COUNT, 10, Duration.ofHours(12))
                    .attach()
                    .withFixedDateSchedule(
                        "UTC",
                        OffsetDateTime.parse("2050-10-12T20:15:10Z"),
                        OffsetDateTime.parse("2051-09-11T16:08:04Z"))
                    .attach()
                    .defineAutoscaleProfile("AutoScaleProfile2")
                    .withMetricBasedScale(1, 5, 3)
                    .defineScaleRule()
                    .withMetricSource(servicePlan.id())
                    .withMetricName("CPUPercentage")
                    .withStatistic(Duration.ofMinutes(10), Duration.ofMinutes(1), MetricStatisticType.AVERAGE)
                    .withCondition(TimeAggregationType.AVERAGE, ComparisonOperationType.LESS_THAN, 20)
                    .withScaleAction(ScaleDirection.DECREASE, ScaleType.EXACT_COUNT, 1, Duration.ofHours(3))
                    .attach()
                    .withRecurrentSchedule("UTC", "12:13", DayOfWeek.FRIDAY)
                    .attach()
                    .withAdminEmailNotification()
                    .withCoAdminEmailNotification()
                    .withCustomEmailsNotification("me@mycorp.com", "you@mycorp.com", "him@mycorp.com")
                    .withAutoscaleDisabled()
                    .create();

            Assertions.assertNotNull(setting);
            Assertions.assertEquals("somesettingZ", setting.name());
            Assertions.assertEquals(servicePlan.id(), setting.targetResourceId());
            Assertions.assertTrue(setting.adminEmailNotificationEnabled());
            Assertions.assertTrue(setting.coAdminEmailNotificationEnabled());
            Assertions.assertFalse(setting.autoscaleEnabled());
            Assertions.assertEquals(3, setting.customEmailsNotification().size());
            Assertions.assertEquals("me@mycorp.com", setting.customEmailsNotification().get(0));
            Assertions.assertEquals("you@mycorp.com", setting.customEmailsNotification().get(1));
            Assertions.assertEquals("him@mycorp.com", setting.customEmailsNotification().get(2));

            Assertions.assertEquals(3, setting.profiles().size());

            AutoscaleProfile tempProfile = setting.profiles().get("Default");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("Default", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(3, tempProfile.maxInstanceCount());
            Assertions.assertEquals(3, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(0, tempProfile.rules().size());
            Assertions.assertNotNull(tempProfile.recurrentSchedule());
            Assertions.assertEquals(RecurrenceFrequency.WEEK, tempProfile.recurrentSchedule().frequency());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(3, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.MONDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.TUESDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.SATURDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(18));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(0));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            tempProfile = setting.profiles().get("AutoScaleProfile1");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile1", tempProfile.name());
            Assertions.assertEquals(1, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(10, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2050-10-12T20:15:10Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2051-09-11T16:08:04Z"), tempProfile.fixedDateSchedule().end());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            ScaleRule rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(10), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.GREATER_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(70, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.INCREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(10, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(12), rule.cooldown());

            tempProfile = setting.profiles().get("AutoScaleProfile2");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile2", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(5, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.FRIDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(12));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(13));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(10), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(20, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(1, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(3), rule.cooldown());

            // GET Autoscale settings and compare
            AutoscaleSetting settingFromGet = monitorManager.autoscaleSettings().getById(setting.id());
            Assertions.assertNotNull(settingFromGet);
            Assertions.assertEquals("somesettingZ", settingFromGet.name());
            Assertions.assertEquals(servicePlan.id(), settingFromGet.targetResourceId());
            Assertions.assertTrue(settingFromGet.adminEmailNotificationEnabled());
            Assertions.assertTrue(settingFromGet.coAdminEmailNotificationEnabled());
            Assertions.assertFalse(settingFromGet.autoscaleEnabled());
            Assertions.assertEquals(3, settingFromGet.customEmailsNotification().size());
            Assertions.assertEquals("me@mycorp.com", settingFromGet.customEmailsNotification().get(0));
            Assertions.assertEquals("you@mycorp.com", settingFromGet.customEmailsNotification().get(1));
            Assertions.assertEquals("him@mycorp.com", settingFromGet.customEmailsNotification().get(2));

            Assertions.assertEquals(3, settingFromGet.profiles().size());

            tempProfile = settingFromGet.profiles().get("Default");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("Default", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(3, tempProfile.maxInstanceCount());
            Assertions.assertEquals(3, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(0, tempProfile.rules().size());
            Assertions.assertNotNull(tempProfile.recurrentSchedule());
            Assertions.assertEquals(RecurrenceFrequency.WEEK, tempProfile.recurrentSchedule().frequency());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(3, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.MONDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.TUESDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.SATURDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(18));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(0));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            tempProfile = settingFromGet.profiles().get("AutoScaleProfile1");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile1", tempProfile.name());
            Assertions.assertEquals(1, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(10, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2050-10-12T20:15:10Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2051-09-11T16:08:04Z"), tempProfile.fixedDateSchedule().end());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(10), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.GREATER_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(70, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.INCREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(10, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(12), rule.cooldown());

            tempProfile = settingFromGet.profiles().get("AutoScaleProfile2");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile2", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(5, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.FRIDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(12));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(13));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(10), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(20, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(1, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(3), rule.cooldown());

            // Update
            setting
                .update()
                .defineAutoscaleProfile("very new profile")
                .withScheduleBasedScale(10)
                .withFixedDateSchedule(
                    "UTC", OffsetDateTime.parse("2030-02-12T20:15:10Z"), OffsetDateTime.parse("2030-02-12T20:45:10Z"))
                .attach()
                .defineAutoscaleProfile("a new profile")
                .withMetricBasedScale(5, 7, 6)
                .defineScaleRule()
                .withMetricSource(servicePlan.id())
                .withMetricName("CPUPercentage")
                .withStatistic(Duration.ofHours(10), Duration.ofHours(1), MetricStatisticType.AVERAGE)
                .withCondition(TimeAggregationType.TOTAL, ComparisonOperationType.LESS_THAN, 6)
                .withScaleAction(ScaleDirection.DECREASE, ScaleType.PERCENT_CHANGE_COUNT, 10, Duration.ofHours(10))
                .attach()
                .attach()
                .updateAutoscaleProfile("AutoScaleProfile2")
                .updateScaleRule(0)
                .withStatistic(Duration.ofMinutes(15), Duration.ofMinutes(1), MetricStatisticType.AVERAGE)
                .parent()
                .withFixedDateSchedule(
                    "UTC", OffsetDateTime.parse("2025-02-02T02:02:02Z"), OffsetDateTime.parse("2025-02-02T03:03:03Z"))
                .defineScaleRule()
                .withMetricSource(servicePlan.id())
                .withMetricName("CPUPercentage")
                .withStatistic(Duration.ofHours(5), Duration.ofHours(3), MetricStatisticType.AVERAGE)
                .withCondition(TimeAggregationType.TOTAL, ComparisonOperationType.LESS_THAN, 50)
                .withScaleAction(ScaleDirection.DECREASE, ScaleType.PERCENT_CHANGE_COUNT, 25, Duration.ofHours(2))
                .attach()
                .withoutScaleRule(1)
                .parent()
                .withoutAutoscaleProfile("AutoScaleProfile1")
                .withAutoscaleEnabled()
                .withoutCoAdminEmailNotification()
                .apply();

            Assertions.assertNotNull(setting);
            Assertions.assertEquals("somesettingZ", setting.name());
            Assertions.assertEquals(servicePlan.id(), setting.targetResourceId());
            Assertions.assertTrue(setting.adminEmailNotificationEnabled());
            Assertions.assertFalse(setting.coAdminEmailNotificationEnabled());
            Assertions.assertTrue(setting.autoscaleEnabled());
            Assertions.assertEquals(3, setting.customEmailsNotification().size());
            Assertions.assertEquals("me@mycorp.com", setting.customEmailsNotification().get(0));
            Assertions.assertEquals("you@mycorp.com", setting.customEmailsNotification().get(1));
            Assertions.assertEquals("him@mycorp.com", setting.customEmailsNotification().get(2));

            Assertions.assertEquals(4, setting.profiles().size());

            Assertions.assertFalse(setting.profiles().containsKey("AutoScaleProfile1"));

            tempProfile = setting.profiles().get("Default");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("Default", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(3, tempProfile.maxInstanceCount());
            Assertions.assertEquals(3, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(0, tempProfile.rules().size());
            Assertions.assertNotNull(tempProfile.recurrentSchedule());
            Assertions.assertEquals(RecurrenceFrequency.WEEK, tempProfile.recurrentSchedule().frequency());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(3, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.MONDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.TUESDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.SATURDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(18));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(0));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            tempProfile = setting.profiles().get("very new profile");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("very new profile", tempProfile.name());
            Assertions.assertEquals(10, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(10, tempProfile.maxInstanceCount());
            Assertions.assertEquals(10, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2030-02-12T20:15:10Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2030-02-12T20:45:10Z"), tempProfile.fixedDateSchedule().end());

            tempProfile = setting.profiles().get("a new profile");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("a new profile", tempProfile.name());
            Assertions.assertEquals(6, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(7, tempProfile.maxInstanceCount());
            Assertions.assertEquals(5, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofHours(10), rule.duration());
            Assertions.assertEquals(Duration.ofHours(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.TOTAL, rule.timeAggregation());
            Assertions.assertEquals(6, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.PERCENT_CHANGE_COUNT, rule.scaleType());
            Assertions.assertEquals(10, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(10), rule.cooldown());

            tempProfile = setting.profiles().get("AutoScaleProfile2");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile2", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(5, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2025-02-02T02:02:02Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2025-02-02T03:03:03Z"), tempProfile.fixedDateSchedule().end());

            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(15), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(20, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(1, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(3), rule.cooldown());

            // List
            settingFromGet = monitorManager.autoscaleSettings().listByResourceGroup(rgName).iterator().next();

            Assertions.assertNotNull(settingFromGet);
            Assertions.assertEquals("somesettingZ", settingFromGet.name());
            Assertions.assertEquals(servicePlan.id(), settingFromGet.targetResourceId());
            Assertions.assertTrue(settingFromGet.adminEmailNotificationEnabled());
            Assertions.assertFalse(settingFromGet.coAdminEmailNotificationEnabled());
            Assertions.assertTrue(settingFromGet.autoscaleEnabled());
            Assertions.assertEquals(3, settingFromGet.customEmailsNotification().size());
            Assertions.assertEquals("me@mycorp.com", settingFromGet.customEmailsNotification().get(0));
            Assertions.assertEquals("you@mycorp.com", settingFromGet.customEmailsNotification().get(1));
            Assertions.assertEquals("him@mycorp.com", settingFromGet.customEmailsNotification().get(2));

            Assertions.assertEquals(4, settingFromGet.profiles().size());

            Assertions.assertFalse(settingFromGet.profiles().containsKey("AutoScaleProfile1"));

            tempProfile = settingFromGet.profiles().get("Default");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("Default", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(3, tempProfile.maxInstanceCount());
            Assertions.assertEquals(3, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(0, tempProfile.rules().size());
            Assertions.assertNotNull(tempProfile.recurrentSchedule());
            Assertions.assertEquals(RecurrenceFrequency.WEEK, tempProfile.recurrentSchedule().frequency());
            Assertions.assertNotNull(tempProfile.recurrentSchedule().schedule());
            Assertions.assertEquals(3, tempProfile.recurrentSchedule().schedule().days().size());
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.MONDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.TUESDAY.toString()));
            Assertions
                .assertTrue(tempProfile.recurrentSchedule().schedule().days().contains(DayOfWeek.SATURDAY.toString()));
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().hours().size());
            Assertions.assertEquals(1, tempProfile.recurrentSchedule().schedule().minutes().size());
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().hours().contains(18));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().minutes().contains(0));
            Assertions.assertTrue(tempProfile.recurrentSchedule().schedule().timeZone().equalsIgnoreCase("UTC"));

            tempProfile = settingFromGet.profiles().get("very new profile");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("very new profile", tempProfile.name());
            Assertions.assertEquals(10, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(10, tempProfile.maxInstanceCount());
            Assertions.assertEquals(10, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2030-02-12T20:15:10Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2030-02-12T20:45:10Z"), tempProfile.fixedDateSchedule().end());

            tempProfile = settingFromGet.profiles().get("a new profile");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("a new profile", tempProfile.name());
            Assertions.assertEquals(6, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(7, tempProfile.maxInstanceCount());
            Assertions.assertEquals(5, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.fixedDateSchedule());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofHours(10), rule.duration());
            Assertions.assertEquals(Duration.ofHours(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.TOTAL, rule.timeAggregation());
            Assertions.assertEquals(6, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.PERCENT_CHANGE_COUNT, rule.scaleType());
            Assertions.assertEquals(10, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(10), rule.cooldown());

            tempProfile = settingFromGet.profiles().get("AutoScaleProfile2");
            Assertions.assertNotNull(tempProfile);
            Assertions.assertEquals("AutoScaleProfile2", tempProfile.name());
            Assertions.assertEquals(3, tempProfile.defaultInstanceCount());
            Assertions.assertEquals(5, tempProfile.maxInstanceCount());
            Assertions.assertEquals(1, tempProfile.minInstanceCount());
            Assertions.assertNull(tempProfile.recurrentSchedule());
            Assertions.assertNotNull(tempProfile.fixedDateSchedule());
            Assertions.assertTrue(tempProfile.fixedDateSchedule().timeZone().equalsIgnoreCase("UTC"));
            Assertions
                .assertEquals(OffsetDateTime.parse("2025-02-02T02:02:02Z"), tempProfile.fixedDateSchedule().start());
            Assertions
                .assertEquals(OffsetDateTime.parse("2025-02-02T03:03:03Z"), tempProfile.fixedDateSchedule().end());

            Assertions.assertNotNull(tempProfile.rules());
            Assertions.assertEquals(1, tempProfile.rules().size());
            rule = tempProfile.rules().get(0);
            Assertions.assertEquals(servicePlan.id(), rule.metricSource());
            Assertions.assertEquals("CPUPercentage", rule.metricName());
            Assertions.assertEquals(Duration.ofMinutes(15), rule.duration());
            Assertions.assertEquals(Duration.ofMinutes(1), rule.frequency());
            Assertions.assertEquals(MetricStatisticType.AVERAGE, rule.frequencyStatistic());
            Assertions.assertEquals(ComparisonOperationType.LESS_THAN, rule.condition());
            Assertions.assertEquals(TimeAggregationType.AVERAGE, rule.timeAggregation());
            Assertions.assertEquals(20, rule.threshold(), 0.001);
            Assertions.assertEquals(ScaleDirection.DECREASE, rule.scaleDirection());
            Assertions.assertEquals(ScaleType.EXACT_COUNT, rule.scaleType());
            Assertions.assertEquals(1, rule.scaleInstanceCount());
            Assertions.assertEquals(Duration.ofHours(3), rule.cooldown());

            // Delete
            monitorManager.autoscaleSettings().deleteById(settingFromGet.id());

            PagedIterable<AutoscaleSetting> emptyList = monitorManager.autoscaleSettings().listByResourceGroup(rgName);
            Assertions.assertEquals(0, TestUtilities.getSize(emptyList));
        } finally {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }
}
