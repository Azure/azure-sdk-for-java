/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;

/**
 * Azure App Service sample for managing web apps.
 *  - app service plan, web app
 *    - Create 2 web apps under the same new app service plan
 *  - domain
 *    - Create a domain
 *  - certificate
 *    - Create a Wildcard SSL certificate for the domain
 *    - update 1st web app to use the domain and a new standard SSL certificate
 *    - update 2nd web app to use the domain and the created wildcard SSL certificate
 *  - slots
 *    - create 2 slots under 2nd web app and bind to the domain and the wildcard SSL certificate
 *    - turn on auto-swap for 2nd slot
 *    - set connection strings to a storage account on production slot and make them sticky
 *  - source control
 *    - bind a simple web app in a public GitHub repo to 2nd slot and have it auto-swapped to production
 *    - Verify the web app has access to the storage account
 *  - Delete a slot
 *  - Delete a web app
 */
public final class ManageAppService {

    private static OkHttpClient httpClient;

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        // Existing resources
        final String domainName     = "jsdk79877.com";
        final String certName       = "wild2crt8b42374211";
        final String domainCertRg   = "rgnemv24d683784f51d";

        // New resources
        final String app1Name       = ResourceNamer.randomResourceName("webapp1", 20);
        final String app2Name       = ResourceNamer.randomResourceName("webapp2", 20);
        final String slot1Name      = ResourceNamer.randomResourceName("slot1", 20);
        final String slot2Name      = ResourceNamer.randomResourceName("slot2", 20);
        final String planName       = ResourceNamer.randomResourceName("jplan", 15);
        final String rgName         = ResourceNamer.randomResourceName("rgNEMV", 24);

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {


                //============================================================
                // Create a web app with a new app service plan

                System.out.println("Creating web app " + app1Name + "...");

                WebApp app1 = azure.webApps()
                        .define(app1Name)
                        .withNewResourceGroup(rgName)
                        .withNewAppServicePlan(planName)
                        .withRegion(Region.US_WEST)
                        .withPricingTier(AppServicePricingTier.STANDARD_S1)
                        .create();

                System.out.println("Created web app " + app1.name());
                Utils.print(app1);

                //============================================================
                // Create a second web app with the same app service plan

                System.out.println("Creating another web app " + app2Name + "...");
                AppServicePlan plan = azure.appServices().appServicePlans().getByGroup(rgName, planName);
                WebApp app2 = azure.webApps()
                        .define(app2Name)
                        .withExistingResourceGroup(rgName)
                        .withExistingAppServicePlan(plan)
                        .create();

                System.out.println("Created web app " + app2.name());
                Utils.print(app2);

                //============================================================
                // Purchase a domain (will be canceled for a full refund)

                System.out.println("Purchasing a domain " + domainName + "...");

                AppServiceDomain domain = azure.appServices().domains()
                        .getByGroup(domainCertRg, domainName);

                System.out.println("Purchased domain " + domain.name());
                Utils.print(domain);

                //============================================================
                // Bind domain to web app 1

                System.out.println("Binding http://" + app1Name + "." + domainName + " to web app " + app1Name + "...");

                app1 = app1.update()
                        .defineHostnameBinding()
                            .withAzureManagedDomain(domain)
                            .withSubDomain(app1Name)
                            .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                            .attach()
                        .apply();

                System.out.println("Finish binding http://" + app1Name + "." + domainName + " to web app " + app1Name + "...");
                Utils.print(app1);

                System.out.println("CURLing http://" + app1Name + "." + domainName);
                System.out.println(curl("http://" + app1Name + "." + domainName));

                //============================================================
                // Purchase a wild card SSL certificate (will be canceled for a full refund)

                AppServiceCertificateOrder certificateOrder = azure.appServices().certificateOrders()
                        .getByGroup(domainCertRg, certName);

                Utils.print(certificateOrder);

                //============================================================
                // Bind domain to web app 2 and turn on wild card SSL

                System.out.println("Binding https://" + app2Name + "." + domainName + " to web app " + app2Name + "...");
                app2 = app2.update()
                        .withManagedHostnameBindings(domain, app2Name)
                        .defineSslBinding()
                            .forHostname(app2Name + "." + domainName)
                            .withExistingAppServiceCertificateOrder(certificateOrder)
                            .withSniBasedSsl()
                            .attach()
                        .apply();

                System.out.println("Finished binding http://" + app2Name + "." + domainName + " to web app " + app2Name + "...");
                Utils.print(app2);

                // Make a call to warm up with web app
                curl("https://" + app2Name + "." + domainName);

                System.out.println("CURLing https://" + app2Name + "." + domainName);
                System.out.println(curl("https://" + app2Name + "." + domainName));

                //============================================================
                // Create 2 slots under web app 2

                // slot1.domainName.com - SSL off - autoswap on
                System.out.println("Creating slot " + slot1Name + "...");

                DeploymentSlot slot1 = app2.deploymentSlots().define(slot1Name)
                        .withBrandNewConfiguration()
                        .withManagedHostnameBindings(domain, slot1Name)
                        .withAutoSwapSlotName("production")
                        .create();

                System.out.println("Created slot " + slot1Name + "...");
                Utils.print(slot1);

                // slot2.domainName.com - SSL on - autoswap on - storage account info
                System.out.println("Creating another slot " + slot2Name + "...");

                DeploymentSlot slot2 = app2.deploymentSlots().define(slot2Name)
                        .withConfigurationFromDeploymentSlot(slot1)
                        .withManagedHostnameBindings(domain, slot2Name)
                        .defineSslBinding()
                            .forHostname(slot2Name + "." + domainName)
                            .withExistingAppServiceCertificateOrder(certificateOrder)
                            .withSniBasedSsl()
                            .attach()
                        .withStickyAppSetting("storageaccount", "account1")
                        .withStickyAppSetting("storageaccountkey", "key1")
                        .create();

                System.out.println("Created slot " + slot2Name + "...");
                Utils.print(slot2);

                //============================================================
                // Update slot 1

                System.out.println("Turning on SSL for slot " + slot1Name + "...");

                slot1 = slot1.update()
                        .withAutoSwapSlotName(null) // this will not affect slot 2
                        .defineSslBinding()
                            .forHostname(slot1Name + "." + domainName)
                            .withExistingAppServiceCertificateOrder(certificateOrder)
                            .withSniBasedSsl()
                            .attach()
                        .apply();

                System.out.println("SSL turned on for slot " + slot1Name + "...");
                Utils.print(slot1);

                //============================================================
                // Deploy public GitHub repo to slot 2

                System.out.println("Deploying public GitHub repo to slot " + slot2Name);

                slot2 = slot2.update()
                        .defineSourceControl()
                            .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
                            .withBranch("master")
                            .attach()
                        .apply();

                System.out.println("Finished deploying public GitHub repo to slot " + slot2Name);
                Utils.print(slot2);

                // Make a call to warm up with web app
                curl("https://" + app2Name + "." + domainName);

                System.out.println("CURLing https://" + app2Name + "." + domainName + ". Should contain auto-swapped slot 2 content.");
                System.out.println(curl("https://" + app2Name + "." + domainName));

            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().deleteByName(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
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

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    static {
        httpClient = new OkHttpClient.Builder().build();
    }
}
