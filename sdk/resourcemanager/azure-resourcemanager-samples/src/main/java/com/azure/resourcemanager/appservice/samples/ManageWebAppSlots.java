// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;

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

    private static final String SUFFIX = ".azurewebsites.net";

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String resourceGroupName     = Utils.randomResourceName(azureResourceManager, "rg", 24);
        final String app1Name       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name       = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String app3Name       = Utils.randomResourceName(azureResourceManager, "webapp3-", 20);
        final String slotName       = "staging";

        try {

            azureResourceManager.resourceGroups().define(resourceGroupName)
                    .withRegion(Region.US_EAST)
                    .create();

            //============================================================
            // Create 3 web apps with 3 new app service plans in different regions

            WebApp app1 = createWebApp(azureResourceManager, app1Name, Region.US_EAST, resourceGroupName);
            WebApp app2 = createWebApp(azureResourceManager, app2Name, Region.EUROPE_WEST, resourceGroupName);
            WebApp app3 = createWebApp(azureResourceManager, app3Name, Region.ASIA_EAST, resourceGroupName);


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
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azureResourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
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
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static WebApp createWebApp(AzureResourceManager azureResourceManager, String appName, Region region, String resourceGroupName) {
        final String appUrl = appName + SUFFIX;

        System.out.println("Creating web app " + appName + " with master branch...");

        WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroupName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
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
        System.out.println(Utils.sendGetRequest("http://" + appUrl));
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
        System.out.println(Utils.sendGetRequest("http://" + slotUrl));

        System.out.println("CURLing " + appUrl + "...");
        System.out.println(Utils.sendGetRequest("http://" + appUrl));
    }

    private static void swapProductionBacktoSlot(DeploymentSlot slot) {
        final String appUrl = slot.parent().name() + SUFFIX;
        System.out.println("Manually swap production slot back to  " + slot.name() + "...");

        slot.swap("production");

        System.out.println("Swapped production slot back to " + slot.name());

        System.out.println("CURLing " + appUrl + "...");
        System.out.println(Utils.sendGetRequest("http://" + appUrl));
    }
}
