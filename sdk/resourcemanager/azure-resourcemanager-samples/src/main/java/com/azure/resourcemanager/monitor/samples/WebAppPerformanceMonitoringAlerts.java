// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertRuleTimeAggregation;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.time.Duration;

/**
 * This sample shows examples of configuring Metric Alerts for WebApp instance performance monitoring through app service plan.
 *  - Create a App Service plan
 *  - Setup an action group to trigger a notification to the heavy performance alerts
 *  - Create auto-mitigated metric alerts for the App Service plan when
 *    - average CPUPercentage on any of Web App instance (where Instance = *) over the last 5 minutes is above 80%
 */
public final class WebAppPerformanceMonitoringAlerts {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgMonitor", 20);

        try {
            // ============================================================
            // Create an App Service plan
            System.out.println("Creating App Service plan");

            AppServicePlan servicePlan = azure.appServicePlans().define("HighlyAvailableWebApps")
                    .withRegion(Region.US_SOUTH_CENTRAL)
                    .withNewResourceGroup(rgName)
                    .withPricingTier(PricingTier.PREMIUM_P1)
                    .withOperatingSystem(OperatingSystem.WINDOWS)
                    .create();

            System.out.println("App Service plan created:");
            Utils.print(servicePlan);

            // ============================================================
            // Create an action group to send notifications in case metric alert condition will be triggered
            ActionGroup ag = azure.actionGroups().define("criticalPerformanceActionGroup")
                    .withExistingResourceGroup(rgName)
                    .defineReceiver("tierOne")
                        .withPushNotification("ops_on_duty@performancemonitoring.com")
                        .withEmail("ops_on_duty@performancemonitoring.com")
                        .withSms("1", "4255655665")
                        .withVoice("1", "2062066050")
                        .withWebhook("https://www.weeneedmorepower.performancemonitoring.com")
                    .attach()
                    .defineReceiver("tierTwo")
                        .withEmail("ceo@performancemonitoring.com")
                        .attach()
                    .create();
            Utils.print(ag);

            // ============================================================
            // Set a trigger to fire each time
            MetricAlert ma = azure.alertRules().metricAlerts().define("Critical performance alert")
                    .withExistingResourceGroup(rgName)
                    .withTargetResource(servicePlan.id())
                    .withPeriod(Duration.ofMinutes(5))
                    .withFrequency(Duration.ofMinutes(1))
                    .withAlertDetails(3, "This alert rule is for U5 - Single resource-multiple criteria - with dimensions - with star")
                    .withActionGroups(ag.id())
                    .defineAlertCriteria("Metric1")
                        .withMetricName("CPUPercentage", "Microsoft.Web/serverfarms")
                        .withCondition(MetricAlertRuleTimeAggregation.TOTAL, MetricAlertRuleCondition.GREATER_THAN, 80)
                        .withDimension("Instance", "*")
                        .attach()
                    .create();
            Utils.print(ma);

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
