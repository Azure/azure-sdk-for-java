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
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import okhttp3.OkHttpClient;
import okhttp3.Request;

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
    private static final String SUFFIX = ".azurewebsites.net";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String resourceGroupName     = SdkContext.randomResourceName("rg", 24);
        final String app1Name       = SdkContext.randomResourceName("webapp1-", 20);
        final String app2Name       = SdkContext.randomResourceName("webapp2-", 20);
        final String app3Name       = SdkContext.randomResourceName("webapp3-", 20);
        final String slotName       = "staging";

        try {

            azure.resourceGroups().define(resourceGroupName)
                    .withRegion(Region.US_EAST)
                    .create();

            //============================================================
            // Create 3 web apps with 3 new app service plans in different regions

            WebApp app1 = createWebApp(azure, app1Name, Region.US_EAST, resourceGroupName);
            WebApp app2 = createWebApp(azure, app2Name, Region.EUROPE_WEST, resourceGroupName);
            WebApp app3 = createWebApp(azure, app3Name, Region.ASIA_EAST, resourceGroupName);


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

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azure.resourceGroups().beginDeleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
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
                    .withLogLevel(LogLevel.BASIC)
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

    private static WebApp createWebApp(Azure azure, String appName, Region region, String resourceGroupName) {
        final String planName = SdkContext.randomResourceName("jplan_", 15);
        final String appUrl = appName + SUFFIX;

        System.out.println("Creating web app " + appName + " with master branch...");

        WebApp app = azure.webApps()
                .define(appName)
                .withExistingResourceGroup(resourceGroupName)
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
