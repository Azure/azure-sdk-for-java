/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
public final class ManageWebAppWithDomainSsl {

    private static OkHttpClient httpClient;

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        // New resources
        final String app1Name       = ResourceNamer.randomResourceName("webapp1-", 20);
        final String app2Name       = ResourceNamer.randomResourceName("webapp2-", 20);
        final String planName       = ResourceNamer.randomResourceName("jplan_", 15);
        final String rgName         = ResourceNamer.randomResourceName("rgNEMV_", 24);
        final String domainName     = ResourceNamer.randomResourceName("jsdkdemo-", 20) + ".com";
        final String certPassword   = "StrongPass!12";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
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

                AppServiceDomain domain = azure.appServices().domains().define(domainName)
                        .withExistingResourceGroup(rgName)
                        .defineRegistrantContact()
                            .withFirstName("Jon")
                            .withLastName("Doe")
                            .withEmail("jondoe@contoso.com")
                            .withAddressLine1("123 4th Ave")
                            .withCity("Redmond")
                            .withStateOrProvince("WA")
                            .withCountry(CountryISOCode.UNITED_STATES)
                            .withPostalCode("98052")
                            .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                            .withPhoneNumber("4258828080")
                            .attach()
                        .withDomainPrivacyEnabled(true)
                        .withAutoRenewEnabled(false)
                        .create();
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

                System.out.println("Finished binding http://" + app1Name + "." + domainName + " to web app " + app1Name);
                Utils.print(app1);

                System.out.println("CURLing http://" + app1Name + "." + domainName + "...");
                System.out.println(curl("http://" + app1Name + "." + domainName));

                //============================================================
                // Create a self-singed SSL certificate

                String pfxPath = ManageWebAppWithDomainSsl.class.getResource("/").getPath() + app2Name + "." + domainName + ".pfx";
                String cerPath = ManageWebAppWithDomainSsl.class.getResource("/").getPath() + app2Name + "." + domainName + ".cer";

                System.out.println("Creating a self-signed certificate " + pfxPath + "...");

                createCertificate(cerPath, pfxPath, domainName, certPassword, "*." + domainName);

                System.out.println("Created self-signed certificate " + pfxPath);

                //============================================================
                // Bind domain to web app 2 and turn on wild card SSL

                System.out.println("Binding https://" + app2Name + "." + domainName + " to web app " + app2Name + "...");

                app2 = app2.update()
                        .withManagedHostnameBindings(domain, app2Name)
                        .defineSslBinding()
                            .forHostname(app2Name + "." + domainName)
                            .withPfxCertificateToUpload(new File(pfxPath), certPassword)
                            .withSniBasedSsl()
                            .attach()
                        .apply();

                System.out.println("Finished binding http://" + app2Name + "." + domainName + " to web app " + app2Name);
                Utils.print(app2);


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

    private static Response curl(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        return httpClient.newCall(request).execute();
    }

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }

    /**
     * This method creates a certificate for given password.
     *
     * @param certPath location of certificate file
     * @param pfxPath location of pfx file
     * @param alias User alias
     * @param password alias password
     * @param cnName domain name
     * @throws Exception exceptions from the creation
     */
    public static void createCertificate(String certPath, String pfxPath,
                                         String alias, String password, String cnName) throws Exception {

        String validityInDays = "3650";
        String keyAlg = "RSA";
        String sigAlg = "SHA1withRSA";
        String keySize = "2048";
        String storeType = "pkcs12";
        String command = "keytool";
        String jdkPath = System.getProperty("java.home");
        if (jdkPath != null && !jdkPath.isEmpty()) {
            jdkPath = jdkPath.concat("\\bin");
        }
        if (new File(jdkPath).isDirectory()) {
            command = String.format("%s%s%s", jdkPath, File.separator, command);
        }

        // Create Pfx file
        String[] commandArgs = {command, "-genkey", "-alias", alias,
                "-keystore", pfxPath, "-storepass", password, "-validity",
                validityInDays, "-keyalg", keyAlg, "-sigalg", sigAlg, "-keysize", keySize,
                "-storetype", storeType, "-dname", "CN=" + cnName, "-ext", "EKU=1.3.6.1.5.5.7.3.1" };
        Utils.cmdInvocation(commandArgs, false);

        // Create cer file i.e. extract public key from pfx
        File pfxFile = new File(pfxPath);
        if (pfxFile.exists()) {
            String[] certCommandArgs = {command, "-export", "-alias", alias,
                    "-storetype", storeType, "-keystore", pfxPath,
                    "-storepass", password, "-rfc", "-file", certPath };
            // output of keytool export command is going to error stream
            // although command is
            // executed successfully, hence ignoring error stream in this case
            Utils.cmdInvocation(certCommandArgs, true);

            // Check if file got created or not
            File cerFile = new File(pfxPath);
            if (!cerFile.exists()) {
                throw new IOException(
                        "Error occurred while creating certificate"
                                + StringUtils.join(" ", certCommandArgs));
            }
        } else {
            throw new IOException("Error occurred while creating certificates"
                    + StringUtils.join(" ", commandArgs));
        }
    }
}
