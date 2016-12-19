/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.QueryStringCachingBehavior;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

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
    private static final String RG_NAME = ResourceNamer.randomResourceName("rgCDN_", 24);
    private static final String SUFFIX = ".azurewebsites.net";
    private static Azure azure;

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String cdnProfileName = Utils.createRandomName("cdnStandardProfile");
        String[] appNames = new String[8];

        try {

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    //.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            azure.resourceGroups().define(RG_NAME)
                    .withRegion(Region.US_CENTRAL)
                    .create();

            try {
                // ============================================================
                // Create 8 websites
                for (int i = 0; i < 8; i++) {
                    appNames[i] = ResourceNamer.randomResourceName("webapp" + (i + 1) + "-", 20);
                }

                // 2 in US
                createWebApp(appNames[0], Region.US_WEST);
                createWebApp(appNames[1], Region.US_EAST);

                // 2 in EU
                createWebApp(appNames[2], Region.EUROPE_WEST);
                createWebApp(appNames[3], Region.EUROPE_NORTH);

                // 2 in Southeast
                createWebApp(appNames[4], Region.ASIA_SOUTHEAST);
                createWebApp(appNames[5], Region.AUSTRALIA_SOUTHEAST);

                // 1 in Brazil
                createWebApp(appNames[6], Region.BRAZIL_SOUTH);

                // 1 in Japan
                createWebApp(appNames[7], Region.JAPAN_WEST);
                // =======================================================================================
                // Create CDN profile using Standard Verizon SKU with endpoints in each region of Web apps.
                System.out.println("Creating a CDN Profile");

                // create Cdn Profile definition object that will let us do a for loop
                // to define all 8 endpoints and then parallelize their creation
                CdnProfile.DefinitionStages.WithStandardCreate profileDefinition = azure.cdnProfiles().define(cdnProfileName)
                        .withRegion(Region.US_CENTRAL)
                        .withExistingResourceGroup(RG_NAME)
                        .withStandardVerizonSku();

                // define all the endpoints. We need to keep track of the last creatable stage
                // to be able to call create on the entire Cdn profile deployment definition.
                Creatable<CdnProfile> cdnCreatable = null;
                for (String webSite : appNames) {
                    cdnCreatable = profileDefinition
                            .defineNewEndpoint()
                                .withOrigin(webSite + SUFFIX)
                                .withHostHeader(webSite + SUFFIX)
                                .withCompressionEnabled(true)
                                .withContentTypeToCompress("application/javascript")
                                .withQueryStringCachingBehavior(QueryStringCachingBehavior.IGNORE_QUERY_STRING)
                            .attach();
                }

                // create profile and then all the defined endpoints in parallel
                CdnProfile profile = cdnCreatable.create();

                // =======================================================================================
                // Load some content (referenced by Web Apps) to the CDN endpoints.
                ArrayList<String> contentToLoad = new ArrayList<>();
                contentToLoad.add("/server.js");
                contentToLoad.add("/pictures/microsoft_logo.png");

                for (CdnEndpoint endpoint : profile.endpoints().values()) {
                    endpoint.loadContent(contentToLoad);
                }

            } catch (Exception f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            } finally {
                if (azure.resourceGroups().getByName(RG_NAME) != null) {
                    System.out.println("Deleting Resource Group: " + RG_NAME);
                    azure.resourceGroups().deleteByName(RG_NAME);
                    System.out.println("Deleted Resource Group: " + RG_NAME);
                } else {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
