/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.ComparisonOperationType;
import com.microsoft.azure.management.monitor.MetricStatisticType;
import com.microsoft.azure.management.monitor.MetricTrigger;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.RecurrenceFrequency;
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleDirection;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.ScaleType;
import com.microsoft.azure.management.monitor.TimeAggregationType;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.util.Arrays;

/**
 * TODO: Todooo to do to do
 */
public final class ManageMonitor {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String resourceGroupName = Utils.createRandomName("rg");

        try {
            // ============================================================
            // Create a resource group for holding all the created resources
            ResourceGroup rg = azure.resourceGroups().define(resourceGroupName)
                .withRegion(Region.US_CENTRAL)
                .create();

            MetricTrigger trigger = azure.autoscaleSettings().defineMetricTrigger("triggerName")
                    .withMetricResourceUri("asd")
                    .withTimeGrain(Period.days(3))
                    .withStatistic(MetricStatisticType.AVERAGE)
                    .withTimeWindow(Period.days(5))
                    .withTimeAggregation(TimeAggregationType.TOTAL)
                    .withOperator(ComparisonOperationType.LESS_THAN)
                    .withThreshold(6.6)
                    .create();

            trigger.update()
                    .withMetricName("New Name")
                    .apply();

            ScaleAction scaleAction = azure.autoscaleSettings().defineScaleAction()
                    .withDirection(ScaleDirection.INCREASE)
                    .withType(ScaleType.PERCENT_CHANGE_COUNT)
                    .withCooldown(Period.months(2))
                    .create();

            scaleAction.update()
                    .withValue("Some value")
                    .apply();

            Recurrence recurrence = azure.autoscaleSettings().defineRecurrence()
                    .withFrequency(RecurrenceFrequency.DAY)
                    .withScheduleTimeZone("PST")
                    .withScheduleHour(3)
                    .withScheduleMinute(5)
                    .withScheduleDay("Monday")
                    .create();

            recurrence.update()
                    .withScheduleHours( Arrays.asList(1, 2, 3))
                    .apply();

            AutoscaleSetting setting = azure.autoscaleSettings().define("somesettingZ")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rg)
                    .defineAutoscaleProfile("AutoScaleProfile1")
                        .withScaleCapacity("0", "10", "5")
                        .defineScaleRule()
                            .defineMetricTrigger("metric trigger name")
                                .withMetricResourceUri("www.contoso.com")
                                .withTimeGrain(Period.days(10))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(1))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(5.4)
                                .attach()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.INCREASE)
                                .withType(ScaleType.EXACT_COUNT)
                                .withCooldown(Period.hours(12))
                                .attach()
                            .attach()
                        .withFixedDate(DateTime.now().minusDays(2), DateTime.now())
                        .defineRecurrence()
                            .withFrequency(RecurrenceFrequency.WEEK)
                            .withScheduleTimeZone("EST")
                            .withScheduleHour(1)
                            .withScheduleMinute(2)
                            .withScheduleDay("Friday")
                            .attach()
                        .attach()
                    .defineAutoscaleProfile("AutoScaleProfile2")
                        .withScaleCapacity("0", "5", "3")
                        .defineScaleRule()
                            .defineMetricTrigger("the Name")
                                .withMetricResourceUri("www.montoso.com")
                                .withTimeGrain(Period.days(3))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(5))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(5.4)
                                .attach()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.DECREASE)
                                .withType(ScaleType.EXACT_COUNT)
                                .withCooldown(Period.hours(3))
                                .attach()
                            .attach()
                        .defineScaleRule()
                            .defineMetricTrigger("trigger Name")
                                .withMetricResourceUri("www.vontoso.com")
                                .withTimeGrain(Period.days(3))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(5))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(6.6)
                                .attach()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.INCREASE)
                                .withType(ScaleType.PERCENT_CHANGE_COUNT)
                                .withCooldown(Period.months(2))
                                .withValue("some value that should be somewhere there")
                                .attach()
                            .attach()
                        .defineRecurrence()
                            .withFrequency(RecurrenceFrequency.DAY)
                            .withScheduleTimeZone("PST")
                            .withScheduleHour(3)
                            .withScheduleMinute(5)
                            .withScheduleDays( Arrays.asList("Monday", "Tuesday", "Saturday"))
                            .attach()
                        .attach()
                    .defineAutoscaleNotification()
                        .withSendToSubscriptionAdministrator()
                        .attach()
                    .withAutoscaleSettingDisabled()
                    .withTargetResourceUri("http://some.web.api.com")
                    .create();

            ScaleRule rule = setting.profiles().get("aaa").rules().get(0);
            setting.update()
                    .defineAutoscaleProfile("a new profile")
                        .withScaleCapacity("5", "7", "6")
                        .defineScaleRule()
                            .defineMetricTrigger("trigger Name")
                                .withMetricResourceUri("www.tontoso.com")
                                .withTimeGrain(Period.days(3))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(5))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(6.6)
                                .attach()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.INCREASE)
                                .withType(ScaleType.PERCENT_CHANGE_COUNT)
                                .withCooldown(Period.months(2))
                                .withValue("value of the values")
                                .attach()
                            .attach()
                        .attach()
                    .updateAutoscaleProfile("AutoScaleProfile2")
                        .withName("AutoScaleProfile12")
                        .updateScaleRule(rule)
                            .updateMetricTrigger()
                                .withStatistic(MetricStatisticType.MAX)
                                .parent()
                            .updateScaleAction()
                                .withValue("ahahahah!")
                                .parent()
                            .parent()
                        .withoutScaleRule(rule)
                        .defineScaleRule()
                            .defineMetricTrigger("trigger Name")
                                .withMetricResourceUri("www.pontoso.com")
                                .withTimeGrain(Period.days(3))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(5))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(6.6)
                                .attach()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.INCREASE)
                                .withType(ScaleType.PERCENT_CHANGE_COUNT)
                                .withCooldown(Period.months(2))
                                .withValue("da da da")
                                .attach()
                            .attach()
                        .withoutFixedDate()
                        .updateRecurrence()
                            .withFrequency(RecurrenceFrequency.YEAR)
                            .parent()
                        .parent()
                    .withoutAutoscaleNotifications()
                    .defineAutoscaleNotification()
                        .withEmailNotificationCustomEmail("sendme@universal.com")
                        .attach()
                    .updateAutoscaleNotification(setting.notifications().get(0))
                        .withoutEmailNotificationCustomEmails()
                        .parent()
                    .withoutAutoscaleProfile("AutoScaleProfile2")
                    .apply();

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            if (azure.resourceGroups().getByName(resourceGroupName) != null) {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azure.resourceGroups().deleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
            } else {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    //.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageMonitor() {
    }
}
