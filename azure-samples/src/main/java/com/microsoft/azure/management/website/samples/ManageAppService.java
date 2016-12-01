/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.website.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.website.AppServiceDomain;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.CertificateProductType;
import com.microsoft.azure.management.website.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.website.WebApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;

/**
 * Azure App Service sample for managing web apps -
 *  - Create 2 web apps under the same new app service plan
 *  - Authorize an application
 *  - Update a key vault
 *    - alter configurations
 *    - change permissions
 *  - Create another key vault
 *  - List key vaults
 *  - Delete a key vault.
 */
public final class ManageAppService {

    private static OkHttpClient httpClient;

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        final String app1Name       = ResourceNamer.randomResourceName("webapp1", 20);
        final String app2Name       = ResourceNamer.randomResourceName("webapp2", 20);
        final String planName       = ResourceNamer.randomResourceName("jplan", 15);
        final String domainName     = ResourceNamer.randomResourceName("jsdk", 10) + ".com";
        final String certName       = ResourceNamer.randomResourceName("democrt", 20);
        final String vaultName      = ResourceNamer.randomResourceName("demovault", 20);
        final String rgName         = ResourceNamer.randomResourceName("rgNEMV", 24);

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

//            azure.webApps().getByGroup("javacsmrg319", "java-webapp-319")
//                    .update()
//                    .defineSourceControl()
//                        .withPublicExternalRepository()
//                        .withGit("https://github.com/jianghaolu/azure-site-test")
//                        .withBranch("master")
//                        .attach()
//                    .apply();

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

                WebApp app2 = azure.webApps()
                        .define(app2Name)
                        .withExistingResourceGroup(rgName)
                        .withExistingAppServicePlan(planName)
                        .create();

                System.out.println("Created web app " + app2.name());
                Utils.print(app2);

                //============================================================
                // Purchase a domain (will be canceled for a full refund)

                System.out.println("Purchasing a domain " + domainName + "...");

                AppServiceDomain domain = azure.appServices().domains()
                        .define(domainName)
                        .withExistingResourceGroup(rgName)
                        .defineRegistrantContact()
                            .withFirstName("Microsoft")
                            .withLastName("Azure")
                            .withEmail("azure@outlook.com")
                            .withAddressLine1("1 Microsoft Way")
                            .withCity("Redmond")
                            .withStateOrProvince("Washington")
                            .withCountry(CountryISOCode.UNITED_STATES)
                            .withPostalCode("98052")
                            .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                            .withPhoneNumber("4258828080")
                            .attach()
                        .create();

                System.out.println("Purchased domain " + domain.name());
                Utils.print(domain);

                //============================================================
                // Bind domain to web app 1

                System.out.println("Binding https://app1." + domainName + " to web app " + app1Name + "...");

                app1.update()
                        .defineHostnameBinding()
                            .withAzureManagedDomain(domain)
                            .withSubDomain("app1")
                            .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                            .attach()
                        .defineSslBinding()
                            .forHostname("app1." + domainName)
                            .withNewAppServiceCertificateOrder(certName, CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL)
                            .withNewKeyVault(vaultName)
                            .withSniBasedSsl()
                            .attach()
                        .apply();

                System.out.println("Finish binding https://app1." + domainName + " to web app " + app1Name + "...");
                Utils.print(app1);

                System.out.println("CURLing https://app1." + domainName);
                System.out.println(curl("https://app1." + domainName));

                //============================================================
                // Bind domain to web app 2 and also purchase a certificate

                System.out.println("Binding www." + domainName + " to web app " + app1Name + "...");

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

    private ManageAppService() {
        httpClient = new OkHttpClient.Builder().build();
    }
}
