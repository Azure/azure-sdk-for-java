/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.samples;

import com.azure.management.Azure;
import com.azure.management.appservice.AppServiceDomain;
import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.CustomHostNameDnsRecordType;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.WebApp;
import com.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;

import java.io.File;


/**
 * Azure App Service sample for managing web apps.
 *  - app service plan, web app
 *    - Create 2 web apps under the same new app service plan
 *  - domain
 *    - Create a domain
 *  - certificate
 *    - Upload a self-signed wildcard certificate
 *    - update both web apps to use the domain and the created wildcard SSL certificate
 */
public final class ManageWebAppWithDomainSsl {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String app2Name       = azure.sdkContext().randomResourceName("webapp2-", 20);
        final String rgName         = azure.sdkContext().randomResourceName("rgNEMV_", 24);
        final String domainName     = azure.sdkContext().randomResourceName("jsdkdemo-", 20) + ".com";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String certPassword   = "StrongPass!12";

        try {
            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + app1Name + "...");

            WebApp app1 = azure.webApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second web app with the same app service plan

            System.out.println("Creating another web app " + app2Name + "...");
            AppServicePlan plan = azure.appServices().appServicePlans().getById(app1.appServicePlanId());
            WebApp app2 = azure.webApps().define(app2Name)
                    .withExistingWindowsPlan(plan)
                    .withExistingResourceGroup(rgName)
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
                        .withCountry(CountryIsoCode.UNITED_STATES)
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

            //============================================================
            // Create a self-singed SSL certificate

            String pfxPath = ManageWebAppWithDomainSsl.class.getResource("/").getPath() + "webapp_" + ManageWebAppWithDomainSsl.class.getSimpleName().toLowerCase() + ".pfx";
            String cerPath = ManageWebAppWithDomainSsl.class.getResource("/").getPath() + "webapp_" + ManageWebAppWithDomainSsl.class.getSimpleName().toLowerCase() + ".cer";

            System.out.println("Creating a self-signed certificate " + pfxPath + "...");

            Utils.createCertificate(cerPath, pfxPath, domainName, certPassword, "*." + domainName);

            System.out.println("Created self-signed certificate " + pfxPath);

            //============================================================
            // Bind domain to web app 2 and turn on wild card SSL for both

            System.out.println("Binding https://" + app1Name + "." + domainName + " to web app " + app1Name + "...");

            app1 = app1.update()
                    .withManagedHostnameBindings(domain, app1Name)
                    .defineSslBinding()
                        .forHostname(app1Name + "." + domainName)
                        .withPfxCertificateToUpload(new File(pfxPath), certPassword)
                        .withSniBasedSsl()
                        .attach()
                    .apply();

            System.out.println("Finished binding http://" + app1Name + "." + domainName + " to web app " + app1Name);
            Utils.print(app1);

            System.out.println("Binding https://" + app2Name + "." + domainName + " to web app " + app2Name + "...");

            app2 = app2.update()
                    .withManagedHostnameBindings(domain, app2Name)
                    .defineSslBinding()
                        .forHostname(app2Name + "." + domainName)
                        .withExistingCertificate(app1.hostNameSslStates().get(app1Name + "." + domainName).thumbprint())
                        .withSniBasedSsl()
                        .attach()
                    .apply();

            System.out.println("Finished binding http://" + app2Name + "." + domainName + " to web app " + app2Name);
            Utils.print(app2);

            return true;
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

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
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
}
