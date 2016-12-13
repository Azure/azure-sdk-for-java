/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create 3 web apps under the same new app service plan:
 *    - 1, 2 are in the same resource group, 3 in a different one
 *    - Stop and start 1, restart 2
 *    - Add Java support to app 3
 *  - List web apps
 *  - Delete a web app
 */
public final class ManageWebAppBasic {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        // New resources
        final String app1Name       = ResourceNamer.randomResourceName("webapp1-", 20);
        final String app2Name       = ResourceNamer.randomResourceName("webapp2-", 20);
        final String app3Name       = ResourceNamer.randomResourceName("webapp3-", 20);
        final String planName       = ResourceNamer.randomResourceName("jplan_", 15);
        final String rg1Name        = ResourceNamer.randomResourceName("rg1NEMV_", 24);
        final String rg2Name        = ResourceNamer.randomResourceName("rg2NEMV_", 24);

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {


                //============================================================
                // Create a web app with a new app service plan

                System.out.println("Creating web app " + app1Name + " in resource group " + rg1Name + "...");

                WebApp app1 = azure.webApps()
                        .define(app1Name)
                        .withNewResourceGroup(rg1Name)
                        .withNewAppServicePlan(planName)
                        .withRegion(Region.US_WEST)
                        .withPricingTier(AppServicePricingTier.STANDARD_S1)
                        .create();

                System.out.println("Created web app " + app1.name());
                Utils.print(app1);

                //============================================================
                // Create a second web app with the same app service plan

                System.out.println("Creating another web app " + app2Name + " in resource group " + rg1Name + "...");
                AppServicePlan plan = azure.appServices().appServicePlans().getByGroup(rg1Name, planName);
                WebApp app2 = azure.webApps()
                        .define(app2Name)
                        .withExistingResourceGroup(rg1Name)
                        .withExistingAppServicePlan(plan)
                        .create();

                System.out.println("Created web app " + app2.name());
                Utils.print(app2);

                //============================================================
                // Create a third web app with the same app service plan, but
                // in a different resource group

                System.out.println("Creating another web app " + app3Name + " in resource group " + rg2Name + "...");
                WebApp app3 = azure.webApps()
                        .define(app3Name)
                        .withNewResourceGroup(rg2Name)
                        .withExistingAppServicePlan(plan)
                        .create();

                System.out.println("Created web app " + app3.name());
                Utils.print(app3);

                //============================================================
                // stop and start app1, restart app 2
                System.out.println("Stopping web app " + app1.name());
                app1.stop();
                System.out.println("Stopped web app " + app1.name());
                Utils.print(app1);
                System.out.println("Starting web app " + app1.name());
                app1.start();
                System.out.println("Started web app " + app1.name());
                Utils.print(app1);
                System.out.println("Restarting web app " + app2.name());
                app2.restart();
                System.out.println("Restarted web app " + app2.name());
                Utils.print(app2);

                //============================================================
                // Configure app 3 to have Java 8 enabled
                System.out.println("Adding Java support to web app " + app3Name + "...");
                app3.update()
                        .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                        .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                        .apply();
                System.out.println("Java supported on web app " + app3Name + "...");

                //=============================================================
                // List web apps

                System.out.println("Printing list of web apps in resource group " + rg1Name + "...");

                for (WebApp webApp : azure.webApps().listByGroup(rg1Name)) {
                    Utils.print(webApp);
                }

                System.out.println("Printing list of web apps in resource group " + rg2Name + "...");

                for (WebApp webApp : azure.webApps().listByGroup(rg2Name)) {
                    Utils.print(webApp);
                }

                //=============================================================
                // Delete a web app

                System.out.println("Deleting web app " + app1Name + "...");
                azure.webApps().deleteByGroup(rg1Name, app1Name);
                System.out.println("Deleted web app " + app1Name + "...");

                System.out.println("Printing list of web apps in resource group " + rg1Name + " again...");
                for (WebApp webApp : azure.webApps().listByGroup(rg1Name)) {
                    Utils.print(webApp);
                }

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

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
