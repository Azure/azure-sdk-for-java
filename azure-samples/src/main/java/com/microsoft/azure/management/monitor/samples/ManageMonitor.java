/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.samples;

import com.google.common.collect.Iterables;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
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
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

            MetricTrigger trigger = azure.autoscaleSettings().createMetricTrigger("triggerName")
                    .withMetricResourceUri("asd")
                    .withTimeGrain(Period.days(3))
                    .withStatistic(MetricStatisticType.AVERAGE)
                    .withTimeWindow(Period.days(5))
                    .withTimeAggregation(TimeAggregationType.TOTAL)
                    .withOperator(ComparisonOperationType.LESS_THAN)
                    .withThreshold(6.6)
                    .apply();

            ScaleAction scaleAction = azure.autoscaleSettings().createScaleAction()
                    .withDirection(ScaleDirection.INCREASE)
                    .withType(ScaleType.PERCENT_CHANGE_COUNT)
                    .withCooldown(Period.months(2))
                    .withValue("asdasdasd")
                    .apply();

            Recurrence recurrence = azure.autoscaleSettings().createRecurrence()
                    .withFrequency(RecurrenceFrequency.DAY)
                    .withScheduleTimeZone("PST")
                    .withScheduleHours(3)
                    .withScheduleMinutes(5)
                    .withScheduleDay("Monday")
                    .withScheduleDay("Tuesday")
                    .withScheduleDay("Saturday")
                    .apply();

            AutoscaleSetting setting = azure.autoscaleSettings().define("somesettingZ")
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rg)
                    .defineAutoscaleProfile("AutoScaleProfile1")
                        .withScaleCapacity("0", "10", "5")
                        .defineScaleRule()
                            .defineMetricTrigger("afonia")
                                .withMetricResourceUri("asd")
                                .withTimeGrain(Period.days(10))
                                .withStatistic(MetricStatisticType.AVERAGE)
                                .withTimeWindow(Period.days(1))
                                .withTimeAggregation(TimeAggregationType.TOTAL)
                                .withOperator(ComparisonOperationType.LESS_THAN)
                                .withThreshold(5.4)
                                .apply()
                            .defineScaleAction()
                                .withDirection(ScaleDirection.INCREASE)
                                .withType(ScaleType.EXACT_COUNT)
                                .withCooldown(Period.hours(12))
                                .apply()
                            .apply()
                        .withTimeWindow(DateTime.now().minusDays(2), DateTime.now())
                        .defineRecurrence()
                            .withFrequency(RecurrenceFrequency.WEEK)
                            .withScheduleTimeZone("EST")
                            .withScheduleHours(1)
                            .withScheduleMinutes(2)
                            .withScheduleDay("Friday")
                            .withScheduleDay("Sunday")
                            .apply()
                        .attach()
                    .defineAutoscaleProfile("AutoScaleProfile2")
                        .withScaleCapacity("0", "5", "3")
                        .defineScaleRule()
                            .withExistingMetricTrigger(new MetricTrigger()
                                    .withMetricName("theName")
                                    .withMetricResourceUri("asd")
                                    .withTimeGrain(Period.days(3))
                                    .withStatistic(MetricStatisticType.AVERAGE)
                                    .withTimeWindow(Period.days(5))
                                    .withTimeAggregation(TimeAggregationType.TOTAL)
                                    .withOperator(ComparisonOperationType.LESS_THAN)
                                    .withThreshold(5.4))
                            .withExistingScaleAction(new ScaleAction()
                                    .withDirection(ScaleDirection.DECREASE)
                                    .withType(ScaleType.EXACT_COUNT)
                                    .withCooldown(Period.hours(3)))
                            .apply()
                        .defineScaleRule()
                            .withExistingMetricTrigger(trigger)
                            .withExistingScaleAction(scaleAction)
                            .apply()
                        .withRecurrence(recurrence)
                        .attach()
                    .defineAutoscaleNotification()
                        .withSendToSubscriptionAdministrator()
                        .attach()
                    .withAutoscaleSettingDisabled()
                    .withTargetResourceUri("http://some.web.api.com")
                    .create();

            ScaleRule rule = setting.profiles().get("aaa").rules().get(0);
            setting.update()
                    .defineAutoscaleProfile("Vazgenidze")
                        .withScaleCapacity("5", "7", "6")
                        .defineScaleRule()
                            .withExistingMetricTrigger(trigger)
                            .withExistingScaleAction(scaleAction)
                            .apply()
                        .attach()
                    .updateAutoscaleProfile("AutoScaleProfile2")
                        .withScaleCapacity("10", "20", "15")
                        .updateScaleRule(rule)
                            .updateMetricTrigger()
                                .withStatistic(MetricStatisticType.MAX)
                                .apply()
                            .updateScaleAction()
                                .withValue("ahahahah!")
                                .apply()
                            .apply()
                        .withoutScaleRule(rule)
                        .withoutTimeWindow()
                        .updateRecurrence()
                            .withFrequency(RecurrenceFrequency.YEAR)
                            .apply()
                        .attach()
                    .withoutAutoscaleNotifications()
                    .defineAutoscaleNotification()
                        .withEmailNotificationCustomEmail("sendme@universal.com")
                        .attach()
                    .updateAutoscaleNotification(setting.notifications().get(0))
                        .withoutEmailNotificationCustomEmails()
                        .attach()
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
