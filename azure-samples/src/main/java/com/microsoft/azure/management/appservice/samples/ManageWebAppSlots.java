/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Azure App Service basic sample for managing web apps.
 *  - Create 3 web apps in 3 different regions
 *  - Deploy to all 3 web apps
 *  - For each of the web apps, create a staging slot
 *  - For each of the web apps, deploy to staging slot
 *  - For each of the web apps, auto-swap to production slot is triggered
 *  - For each of the web apps, swap back (something goes wrong)
 */
public final class ManageWebAppSlots {

    private static OkHttpClient httpClient;
    private static Azure azure;
    private static final String RG_NAME = ResourceNamer.randomResourceName("rg1NEMV_", 24);
    private static final String SUFFIX = ".azurewebsites.net";

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        // New resources
        final String app1Name       = ResourceNamer.randomResourceName("webapp1-", 20);
        final String app2Name       = ResourceNamer.randomResourceName("webapp2-", 20);
        final String app3Name       = ResourceNamer.randomResourceName("webapp3-", 20);
        final String slotName       = "staging";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {

                azure.resourceGroups().define(RG_NAME)
                        .withRegion(Region.US_WEST)
                        .create();

                //============================================================
                // Create 3 web apps with 3 new app service plans in different regions

                WebApp app1 = createWebApp(app1Name, Region.US_WEST);
                WebApp app2 = createWebApp(app2Name, Region.EUROPE_WEST);
                WebApp app3 = createWebApp(app3Name, Region.ASIA_EAST);


                //============================================================
                // Create a deployment slot under each web app with auto swap

                DeploymentSlot slot1 = createSlot(slotName, app1);
                DeploymentSlot slot2 = createSlot(slotName, app2);
                DeploymentSlot slot3 = createSlot(slotName, app3);

                //============================================================
                // Deploy the staging branch to the slot

                deployToStaging(slot1);
                deployToStaging(slot2);
                deployToStaging(slot3);

                // swap back
                swapProductionBacktoSlot(slot1);
                swapProductionBacktoSlot(slot2);
                swapProductionBacktoSlot(slot3);

            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    System.out.println("Deleting Resource Group: " + RG_NAME);
                    azure.resourceGroups().beginDeleteByName(RG_NAME);
                    System.out.println("Deleted Resource Group: " + RG_NAME);
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

    private static WebApp createWebApp(String appName, Region region) {
        final String planName = ResourceNamer.randomResourceName("jplan_", 15);
        final String appUrl = appName + SUFFIX;

        System.out.println("Creating web app " + appName + " with master branch...");

        WebApp app = azure.webApps()
                .define(appName)
                .withExistingResourceGroup(RG_NAME)
                .withNewAppServicePlan(planName)
                .withRegion(region)
                .withPricingTier(AppServicePricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                .defineSourceControl()
                    .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test.git")
                    .withBranch("master")
                    .attach()
                .create();

        System.out.println("Created web app " + app.name());
        Utils.print(app);

        System.out.println("CURLing " + appUrl + "...");
        System.out.println(curl("http://" + appUrl));
        return app;
    }

    private static DeploymentSlot createSlot(String slotName, WebApp app) {
        System.out.println("Creating a slot " + slotName + " with auto swap turned on...");

        DeploymentSlot slot = app.deploymentSlots()
                .define(slotName)
                .withConfigurationFromParent()
                .withAutoSwapSlotName("production")
                .create();

        System.out.println("Created slot " + slot.name());
        Utils.print(slot);
        return slot;
    }

    private static void deployToStaging(DeploymentSlot slot) {
        final String slotUrl = slot.parent().name() + "-" + slot.name() + SUFFIX;
        final String appUrl = slot.parent().name() + SUFFIX;
        System.out.println("Deploying staging branch to slot " + slot.name() + "...");

        slot.update()
                .defineSourceControl()
                .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test.git")
                .withBranch("staging")
                .attach()
                .apply();

        System.out.println("Deployed staging branch to slot " + slot.name());

        System.out.println("CURLing " + slotUrl + "...");
        System.out.println(curl("http://" + slotUrl));

        System.out.println("CURLing " + appUrl + "...");
        System.out.println(curl("http://" + appUrl));
    }

    private static void swapProductionBacktoSlot(DeploymentSlot slot) {
        final String appUrl = slot.parent().name() + SUFFIX;
        System.out.println("Manually swap production slot back to  " + slot.name() + "...");

        slot.swap("production");

        System.out.println("Swapped production slot back to " + slot.name());

        System.out.println("CURLing " + appUrl + "...");
        System.out.println(curl("http://" + appUrl));
    }

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }
}
