// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;

/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 3 function apps under the same new app service plan:
 *    - 1, 2 are in the same resource group, 3 in a different one
 *    - 1, 3 are under the same consumption plan, 2 under a basic app service plan
 *  - List function apps
 *  - Delete a function app
 */
public final class ManageFunctionAppBasic {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String app2Name       = azure.sdkContext().randomResourceName("webapp2-", 20);
        final String app3Name       = azure.sdkContext().randomResourceName("webapp3-", 20);
        final String rg1Name        = azure.sdkContext().randomResourceName("rg1NEMV_", 24);
        final String rg2Name        = azure.sdkContext().randomResourceName("rg2NEMV_", 24);

        try {


            //============================================================
            // Create a function app with a new app service plan

            System.out.println("Creating function app " + app1Name + " in resource group " + rg1Name + "...");

            FunctionApp app1 = azure.functionApps()
                    .define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rg1Name)
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second function app with the same app service plan

            System.out.println("Creating another function app " + app2Name + " in resource group " + rg1Name + "...");
            AppServicePlan plan = azure.appServicePlans().getById(app1.appServicePlanId());
            FunctionApp app2 = azure.functionApps()
                    .define(app2Name)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rg1Name)
                    .withNewAppServicePlan(PricingTier.BASIC_B1)
                    .create();

            System.out.println("Created function app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Create a third function app with the same app service plan, but
            // in a different resource group

            System.out.println("Creating another function app " + app3Name + " in resource group " + rg2Name + "...");
            FunctionApp app3 = azure.functionApps()
                    .define(app3Name)
                    .withExistingAppServicePlan(plan)
                    .withNewResourceGroup(rg2Name)
                    .create();

            System.out.println("Created function app " + app3.name());
            Utils.print(app3);

            //============================================================
            // stop and start app1, restart app 2
            System.out.println("Stopping function app " + app1.name());
            app1.stop();
            System.out.println("Stopped function app " + app1.name());
            Utils.print(app1);
            System.out.println("Starting function app " + app1.name());
            app1.start();
            System.out.println("Started function app " + app1.name());
            Utils.print(app1);
            System.out.println("Restarting function app " + app2.name());
            app2.restart();
            System.out.println("Restarted function app " + app2.name());
            Utils.print(app2);

            //=============================================================
            // List function apps

            System.out.println("Printing list of function apps in resource group " + rg1Name + "...");

            for (FunctionApp functionApp : azure.functionApps().listByResourceGroup(rg1Name)) {
                Utils.print(functionApp);
            }

            System.out.println("Printing list of function apps in resource group " + rg2Name + "...");

            for (FunctionApp functionApp : azure.functionApps().listByResourceGroup(rg2Name)) {
                Utils.print(functionApp);
            }

            //=============================================================
            // Delete a function app

            System.out.println("Deleting function app " + app1Name + "...");
            azure.functionApps().deleteByResourceGroup(rg1Name, app1Name);
            System.out.println("Deleted function app " + app1Name + "...");

            System.out.println("Printing list of function apps in resource group " + rg1Name + " again...");
            for (FunctionApp functionApp : azure.functionApps().listByResourceGroup(rg1Name)) {
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
                System.out.println("Deleting Resource Group: " + rg2Name);
                azure.resourceGroups().beginDeleteByName(rg2Name);
                System.out.println("Deleted Resource Group: " + rg2Name);
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
