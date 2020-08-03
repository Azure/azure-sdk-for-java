// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.monitor.models.AutoscaleSetting;
import com.azure.resourcemanager.monitor.models.ComparisonOperationType;
import com.azure.resourcemanager.monitor.models.DayOfWeek;
import com.azure.resourcemanager.monitor.models.MetricStatisticType;
import com.azure.resourcemanager.monitor.models.ScaleDirection;
import com.azure.resourcemanager.monitor.models.ScaleType;
import com.azure.resourcemanager.monitor.models.TimeAggregationType;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

/**
 * This sample shows how to programmatically implement scenario described <a href="https://docs.microsoft.com/en-us/azure/monitoring-and-diagnostics/monitor-tutorial-autoscale-performance-schedule">here</a>.
 *  - Create a Web App and App Service Plan
 *  - Configure autoscale rules for scale-in and scale out based on the number of requests a Web App receives
 *  - Trigger a scale-out action and watch the number of instances increase
 *  - Trigger a scale-in action and watch the number of instances decrease
 *  - Clean up your resources
 */
public final class AutoscaleSettingsBasedOnPerformanceOrSchedule {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String webappName = azure.sdkContext().randomResourceName("MyTestScaleWebApp", 20);
        final String autoscaleSettingsName = azure.sdkContext().randomResourceName("autoscalename1", 20);
        final String rgName = azure.sdkContext().randomResourceName("myResourceGroup", 20);

        try {
            // ============================================================
            // Create a Web App and App Service Plan
            System.out.println("Creating a web app and service plan");

            WebApp webapp = azure.webApps().define(webappName)
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.PREMIUM_P1)
                    .create();

            System.out.println("Created a web app:");
            Utils.print(webapp);

            // ============================================================
            // Configure autoscale rules for scale-in and scale out based on the number of requests a Web App receives
            AutoscaleSetting scaleSettings = azure.autoscaleSettings().define(autoscaleSettingsName)
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withExistingResourceGroup(rgName)
                    .withTargetResource(webapp.appServicePlanId())
                    // defining Default profile. Note: first created profile is always the default one.
                    .defineAutoscaleProfile("Default profile")
                        .withFixedInstanceCount(1)
                        .attach()
                    // defining Monday to Friday profile
                    .defineAutoscaleProfile("Monday to Friday")
                        .withMetricBasedScale(1, 2, 1)
                        // Create a scale-out rule
                        .defineScaleRule()
                            .withMetricSource(webapp.id())
                            .withMetricName("Requests")
                            .withStatistic(Duration.ofMinutes(5), MetricStatisticType.SUM)
                            .withCondition(TimeAggregationType.TOTAL, ComparisonOperationType.GREATER_THAN, 10)
                            .withScaleAction(ScaleDirection.INCREASE, ScaleType.CHANGE_COUNT, 1, Duration.ofMinutes(5))
                            .attach()
                        // Create a scale-in rule
                        .defineScaleRule()
                            .withMetricSource(webapp.id())
                            .withMetricName("Requests")
                            .withStatistic(Duration.ofMinutes(10), MetricStatisticType.AVERAGE)
                            .withCondition(TimeAggregationType.TOTAL, ComparisonOperationType.LESS_THAN, 5)
                            .withScaleAction(ScaleDirection.DECREASE, ScaleType.CHANGE_COUNT, 1, Duration.ofMinutes(5))
                            .attach()
                        // Create profile schedule
                        .withRecurrentSchedule("Pacific Standard Time", "09:00", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
                        .attach()
                    // define end time for the "Monday to Friday" profile specified above
                    .defineAutoscaleProfile("{\"name\":\"Default\",\"for\":\"Monday to Friday\"}")
                        .withScheduleBasedScale(1)
                        .withRecurrentSchedule("Pacific Standard Time", "18:30", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
                        .attach()
                    .create();

            System.out.println("Auto-scale Setting: " + scaleSettings.id());

            String deployedWebAppUrl = "https://" + webapp.hostnames().iterator().next() + "/";

            // Trigger scale-out action
            for (int i = 0; i < 11; i++) {
                SdkContext.sleep(5000);
                Utils.curl(deployedWebAppUrl);
            }

            // Now you can browse the history of autoscale form the azure portal
            // 1. Open the App Service Plan.
            // 2. From the left-hand navigation pane, select the Monitor option. Once the page loads select the Autoscale tab.
            // 3. From the list, select the App Service Plan used throughout this tutorial.
            // 4. On the autoscale setting, click the Run history tab.
            // 5. You see a chart reflecting the instance count of the App Service Plan over time.
            // 6. In a few minutes, the instance count should rise from 1, to 2.
            // 7. Under the chart, you see the activity log entries for each scale action taken by this autoscale setting.

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            if (azure.resourceGroups().getByName(rgName) != null) {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
