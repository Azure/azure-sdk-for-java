/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure App Service basic sample for managing function apps.
 * - Create 3 function apps in the same resource group
 * - 1 & 2 are under the same consumption plan
 * - 3 is in a basic app service plan
 * - 2 is created with a sample function (in C#)
 * - 1 is updated to have a daily quota and latest runtime
 * - List function apps
 * - Delete a function app
 */
public final class ManageFunctionApp {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String app1Name       = SdkContext.randomResourceName("funcapp-", 20);
        final String app2Name       = SdkContext.randomResourceName("funcapp-", 20);
        final String app3Name       = SdkContext.randomResourceName("funcapp-", 20);
        final String rg1Name        = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with a new consumption plan

            System.out.println("Creating function app " + app1Name + " in resource group " + rg1Name + "...");

            FunctionApp app1 = azure.appServices().functionApps()
                    .define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rg1Name)
                    .withNewConsumptionPlan()
                    .withRuntimeVersion("1")
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second function app with the same consumption plan

            System.out.println("Creating function app " + app2Name + " in resource group " + rg1Name + "...");

            AppServicePlan consumptionPlan = azure.appServices().appServicePlans().getById(app1.appServicePlanId());

            FunctionApp app2 = azure.appServices().functionApps()
                    .define(app2Name)
                    .withExistingAppServicePlan(consumptionPlan)
                    .withExistingResourceGroup(rg1Name)
                    .withExistingStorageAccount(app1.storageAccount())
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/azure-appservice-samples/AzureFunctions-Samples.git")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created function app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Create a third function app with the a new app service plan

            System.out.println("Creating function app " + app3Name + " in resource group " + rg1Name + "...");

            FunctionApp app3 = azure.appServices().functionApps()
                    .define(app3Name)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rg1Name)
                    .withNewAppServicePlan(PricingTier.BASIC_B1)
                    .withExistingStorageAccount(app1.storageAccount())
                    .create();

            System.out.println("Created function app " + app3.name());
            Utils.print(app3);

            //============================================================
            // Update the first function app with latest runtime and 1GB-sec quota

            System.out.println("Updating function app " + "...");

            app1.update()
                    .withDailyUsageQuota(1)
                    .withRuntimeVersion("latest")
                    .apply();

            System.out.println("Updated function app " + app1.name());
            Utils.print(app1);

            //=============================================================
            // List function apps

            System.out.println("Printing list of function apps in resource group " + rg1Name + "...");

            for (FunctionApp functionApp : azure.appServices().functionApps().listByResourceGroup(rg1Name)) {
                Utils.print(functionApp);
            }

            //=============================================================
            // Delete a function app

            System.out.println("Deleting function app " + app1Name + "...");
            azure.webApps().deleteByResourceGroup(rg1Name, app1Name);
            System.out.println("Deleted function app " + app1Name + "...");

            System.out.println("Printing list of web apps in resource group " + rg1Name + " again...");
            for (FunctionApp functionApp : azure.appServices().functionApps().listByResourceGroup(rg1Name)) {
                Utils.print(functionApp);
            }
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rg1Name);
                azure.resourceGroups().beginDeleteByName(rg1Name);
                System.out.println("Deleted Resource Group: " + rg1Name);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
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

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
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
}
