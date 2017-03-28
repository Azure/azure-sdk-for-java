/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Azure CDN sample for managing CDN profiles:
 * - Create 8 web apps in 8 regions:
 *    * 2 in US
 *    * 2 in EU
 *    * 2 in Southeast
 *    * 1 in Brazil
 *    * 1 in Japan
 * - Create CDN profile using Standard Verizon SKU with endpoints in each region of Web apps.
 * - Load some content (referenced by Web Apps) to the CDN endpoints.
 */
public final class ManageCdn {
    private static OkHttpClient httpClient;
    private static final String SUFFIX = ".azurewebsites.net";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String resourceGroupName = Utils.createRandomName("rg");
        final String cdnProfileName = Utils.createRandomName("cdnStandardProfile");
        String[] appNames = new String[8];

        try {
            // ============================================================
            // Create a resource group for holding all the created resources
            azure.resourceGroups().define(resourceGroupName)
                .withRegion(Region.US_CENTRAL)
                .create();

            // ============================================================
            // Create 8 websites
            for (int i = 0; i < 8; i++) {
                appNames[i] = SdkContext.randomResourceName("webapp" + (i + 1) + "-", 20);
            }

            // 2 in US
            createWebApp(appNames[0], Region.US_WEST, azure, resourceGroupName);
            createWebApp(appNames[1], Region.US_EAST, azure, resourceGroupName);

            // 2 in EU
            createWebApp(appNames[2], Region.EUROPE_WEST, azure, resourceGroupName);
            createWebApp(appNames[3], Region.EUROPE_NORTH, azure, resourceGroupName);

            // 2 in Southeast
            createWebApp(appNames[4], Region.ASIA_SOUTHEAST, azure, resourceGroupName);
            createWebApp(appNames[5], Region.AUSTRALIA_SOUTHEAST, azure, resourceGroupName);

            // 1 in Brazil
            createWebApp(appNames[6], Region.BRAZIL_SOUTH, azure, resourceGroupName);

            // 1 in Japan
            createWebApp(appNames[7], Region.JAPAN_WEST, azure, resourceGroupName);

            // =======================================================================================
            // Create CDN profile using Standard Verizon SKU with endpoints in each region of Web apps.
            System.out.println("Creating a CDN Profile");

            // Create CDN Profile definition object that will let us do a for loop
            // to define all 8 endpoints and then parallelize their creation
            CdnProfile.DefinitionStages.WithStandardCreate profileDefinition = azure.cdnProfiles().define(cdnProfileName)
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
            ArrayList<String> contentToLoad = new ArrayList<>();
            contentToLoad.add("/server.js");
            contentToLoad.add("/pictures/microsoft_logo.png");

            for (CdnEndpoint endpoint : profile.endpoints().values()) {
                endpoint.loadContent(contentToLoad);
            }
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            if (azure.resourceGroups().getByName(resourceGroupName) != null) {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azure.resourceGroups().deleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
            } else {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    //.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
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

    private static WebApp createWebApp(String appName, Region region, Azure azure, String resourceGroupName) {
        final String planName = SdkContext.randomResourceName("jplan_", 15);
        final String appUrl = appName + SUFFIX;

        System.out.println("Creating web app " + appName + " with master branch...");

        WebApp app = azure.webApps()
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
        System.out.println(curl("http://" + appUrl));
        return app;
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

    private ManageCdn() {
    }
}
