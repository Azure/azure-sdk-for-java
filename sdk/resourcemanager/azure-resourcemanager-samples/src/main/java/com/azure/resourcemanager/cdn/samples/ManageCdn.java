// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.QueryStringCachingBehavior;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.samples.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Azure CDN sample for managing CDN profile.
 * - Create 8 web apps in 8 regions:
 *    * 2 in US
 *    * 2 in EU
 *    * 2 in Southeast
 *    * 1 in Brazil
 *    * 1 in Japan
 * - Create CDN profile using Standard Verizon SKU with endpoints in each region of Web apps.
 * - Load some content (referenced by Web Apps) to the CDN endpoints.
 */
public class ManageCdn {

    private static final String SUFFIX = ".azurewebsites.net";

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String resourceGroupName = Utils.randomResourceName(azureResourceManager, "rg", 20);
        final String cdnProfileName = Utils.randomResourceName(azureResourceManager, "cdnStandardProfile", 20);
        String[] appNames = new String[8];

        try {
            // ============================================================
            // Create a resource group for holding all the created resources
            azureResourceManager.resourceGroups().define(resourceGroupName)
                .withRegion(Region.US_CENTRAL)
                .create();

            // ============================================================
            // Create 8 websites
            for (int i = 0; i < 8; i++) {
                appNames[i] = Utils.randomResourceName(azureResourceManager, "webapp" + (i + 1) + "-", 20);
            }

            // 2 in US
            createWebApp(appNames[0], Region.US_WEST, azureResourceManager, resourceGroupName);
            createWebApp(appNames[1], Region.US_EAST, azureResourceManager, resourceGroupName);

            // 2 in EU
            createWebApp(appNames[2], Region.EUROPE_WEST, azureResourceManager, resourceGroupName);
            createWebApp(appNames[3], Region.EUROPE_NORTH, azureResourceManager, resourceGroupName);

            // 2 in Southeast
            createWebApp(appNames[4], Region.ASIA_SOUTHEAST, azureResourceManager, resourceGroupName);
            createWebApp(appNames[5], Region.AUSTRALIA_SOUTHEAST, azureResourceManager, resourceGroupName);

            // 1 in Brazil
            createWebApp(appNames[6], Region.BRAZIL_SOUTH, azureResourceManager, resourceGroupName);

            // 1 in Japan
            createWebApp(appNames[7], Region.JAPAN_WEST, azureResourceManager, resourceGroupName);

            // =======================================================================================
            // Create CDN profile using Standard Verizon SKU with endpoints in each region of Web apps.
            System.out.println("Creating a CDN Profile");

            // Create CDN Profile definition object that will let us do a for loop
            // to define all 8 endpoints and then parallelize their creation
            CdnProfile.DefinitionStages.WithStandardCreate profileDefinition = azureResourceManager.cdnProfiles().define(cdnProfileName)
                .withRegion(Region.US_CENTRAL)
                .withExistingResourceGroup(resourceGroupName)
                .withStandardVerizonSku();

            // Define all the endpoints. We need to keep track of the last creatable stage
            // to be able to call create on the entire Cdn profile deployment definition.
            Creatable<CdnProfile> cdnCreatable = null;
            for (String webSite : appNames) {
                cdnCreatable = profileDefinition.defineNewEndpoint()
                    .withOrigin(webSite + SUFFIX)
                    .withHostHeader(webSite + SUFFIX)
                    .withCompressionEnabled(true)
                    .withContentTypeToCompress("application/javascript")
                    .withQueryStringCachingBehavior(QueryStringCachingBehavior.IGNORE_QUERY_STRING)
                    .attach();
            }

            // Create profile and then all the defined endpoints in parallel
            CdnProfile profile = cdnCreatable.create();

            // =======================================================================================
            // Load some content (referenced by Web Apps) to the CDN endpoints.
            Set<String> contentToLoad = new HashSet<>();
            contentToLoad.add("/server.js");
            contentToLoad.add("/pictures/microsoft_logo.png");

            for (CdnEndpoint endpoint : profile.endpoints().values()) {
                endpoint.loadContent(contentToLoad);
            }
            return true;
        } finally {
            if (azureResourceManager.resourceGroups().getByName(resourceGroupName) != null) {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azureResourceManager.resourceGroups().deleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
            } else {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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

    private static WebApp createWebApp(String appName, Region region, AzureResourceManager azureResourceManager, String resourceGroupName) {
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
}
