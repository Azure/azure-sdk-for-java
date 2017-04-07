/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure App Service basic sample for managing Linux web apps.
 *  - Create 2 web apps under the same linux plan and resource group:
 *    - 1 with PHP stack and a simple PHP site
 *    - 2 with tomcat docker hub image and deployed with a Java web application
 *  - List web apps
 *  - Delete a web app
 */
public final class ManageLinuxWebApp {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String app1Name       = SdkContext.randomResourceName("webapp1-", 20);
        final String app2Name       = SdkContext.randomResourceName("webapp2-", 20);
        final String rg1Name        = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a web app with a new app service plan, deployed with express server

            System.out.println("Creating web app " + app1Name + " in resource group " + rg1Name + "...");

            WebApp app1 = azure.webApps()
                    .define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rg1Name)
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withBuiltInImage(RuntimeStack.PHP_7_0_6)
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/azure-appservice-samples/php-get-started")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a web app with the same app service plan, deployed with tomcat

            AppServicePlan plan = azure.appServices().appServicePlans().getById(app1.appServicePlanId());

            WebApp app2 = azure.webApps()
                    .define(app2Name)
                    .withExistingLinuxPlan(plan)
                    .withExistingResourceGroup(rg1Name)
                    .withPublicDockerHubImage("tomcat:8-jre8")
                    .withStartUpCommand("/bin/bash -c \"sed -ie 's/appBase=\\\"webapps\\\"/appBase=\\\"\\\\/home\\\\/site\\\\/wwwroot\\\\/webapps\\\"/g' conf/server.xml && catalina.sh run\"")
                    .withAppSetting("PORT", "8080")
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/azure-appservice-samples/java-get-started")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created web app " + app2.name());
            Utils.print(app2);


            //=============================================================
            // List web apps

            System.out.println("Printing list of web apps in resource group " + rg1Name + "...");

            for (WebApp webApp : azure.webApps().listByResourceGroup(rg1Name)) {
                Utils.print(webApp);
            }

            //=============================================================
            // Delete a web app

            System.out.println("Deleting web app " + app1Name + "...");
            azure.webApps().deleteByResourceGroup(rg1Name, app1Name);
            System.out.println("Deleted web app " + app1Name + "...");

            System.out.println("Printing list of web apps in resource group " + rg1Name + " again...");
            for (WebApp webApp : azure.webApps().listByResourceGroup(rg1Name)) {
                Utils.print(webApp);
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
