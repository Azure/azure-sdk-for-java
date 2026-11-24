// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure App Service sample for managing deployment slots.
 *  - Create a web app
 *  - Create a staging deployment slot with configuration inherited from the parent
 *  - Update a setting in the staging slot
 *  - Swap the staging slot into production, then swap back
 */
public final class ManageWebAppSlots {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rg", 24);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp-", 20);
        final String slotName = "staging";

        try {
            // Create a web app running Tomcat on a Windows plan.
            // HTTPS-only is enforced; minimum TLS 1.2 and FTPS-only are already the App Service defaults.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_11)
                .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                .withHttpsOnly(true)
                .create();

            // Create a staging deployment slot that inherits the production configuration.
            DeploymentSlot slot = app.deploymentSlots()
                .define(slotName)
                .withConfigurationFromParent()
                .create();

            // Apply a slot-specific setting.
            slot.update().withAppSetting("slot.setting", "staging-value").apply();

            // Swap the staging slot into production, then swap back.
            slot.swap("production");
            slot.swap("production");

            System.out.println("Managed deployment slot " + slotName + " for web app " + app.name());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageWebAppSlots() {
    }
}
