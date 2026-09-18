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
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficRoutingMethod;

/**
 * Azure App Service sample for scaling a web app across multiple regions with high availability.
 *  - Create three app service plans in three different regions
 *  - Create a web app in each region
 *  - Put a Traffic Manager profile in front of the web apps
 *  - Scale up the app service plans
 */
public final class ScaleWebAppWithTrafficManager {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgNEMV_", 24);
        final String tmName = SampleUtils.randomResourceName(azureResourceManager, "jsdktm-", 20);

        try {
            azureResourceManager.resourceGroups().define(rgName).withRegion(Region.US_WEST).create();

            // Create web apps (each on its own Linux plan) in three regions.
            WebApp app1 = createWebApp(azureResourceManager, rgName, "webapp1", Region.US_WEST);
            WebApp app2 = createWebApp(azureResourceManager, rgName, "webapp2", Region.EUROPE_WEST);
            WebApp app3 = createWebApp(azureResourceManager, rgName, "webapp3", Region.ASIA_SOUTHEAST);

            // Route traffic to the web apps by priority using Traffic Manager.
            TrafficManagerProfile trafficManager = azureResourceManager.trafficManagerProfiles()
                .define(tmName)
                .withExistingResourceGroup(rgName)
                .withLeafDomainLabel(tmName)
                .withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY)
                .defineAzureTargetEndpoint("endpoint1")
                .toResourceId(app1.id())
                .withRoutingPriority(1)
                .attach()
                .defineAzureTargetEndpoint("endpoint2")
                .toResourceId(app2.id())
                .withRoutingPriority(2)
                .attach()
                .defineAzureTargetEndpoint("endpoint3")
                .toResourceId(app3.id())
                .withRoutingPriority(3)
                .attach()
                .create();

            System.out.println("Created Traffic Manager: " + trafficManager.fqdn());

            // Scale up the app service plan of the primary web app.
            AppServicePlan plan1 = azureResourceManager.appServicePlans().getById(app1.appServicePlanId());
            plan1.update().withCapacity(plan1.capacity() * 2).apply();

            System.out.println("Scaled up app service plan " + plan1.name());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    private static WebApp createWebApp(AzureResourceManager azureResourceManager, String rgName, String namePrefix,
        Region region) {
        String appName = SampleUtils.randomResourceName(azureResourceManager, namePrefix + "-", 20);
        String planName = SampleUtils.randomResourceName(azureResourceManager, "plan-", 15);

        AppServicePlan plan = azureResourceManager.appServicePlans()
            .define(planName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withPricingTier(PricingTier.STANDARD_S1)
            .withOperatingSystem(OperatingSystem.LINUX)
            .create();

        return azureResourceManager.webApps()
            .define(appName)
            .withExistingLinuxPlan(plan)
            .withExistingResourceGroup(rgName)
            .withBuiltInImage(RuntimeStack.JAVA_17_JAVA17)
            .withHttpsOnly(true)
            .create();
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

    private ScaleWebAppWithTrafficManager() {
    }
}
