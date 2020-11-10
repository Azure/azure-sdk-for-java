// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Azure App Service sample for managing function apps.
 *  - app service plan, function app
 *    - Create 2 function apps under the same new app service plan
 *  - domain
 *    - Create a domain
 *  - certificate
 *    - Upload a self-signed wildcard certificate
 *    - update both function apps to use the domain and the created wildcard SSL certificate
 */
public final class ManageFunctionAppWithDomainSsl {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        // New resources
        final String app1Name       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name       = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rgNEMV_", 24);
        final String domainName     = Utils.randomResourceName(azureResourceManager, "jsdkdemo-", 20) + ".com";
        final String certPassword   = Utils.password();

        try {
            //============================================================
            // Create a function app with a new app service plan

            System.out.println("Creating function app " + app1Name + "...");

            FunctionApp app1 = azureResourceManager.functionApps().define(app1Name)
                    .withRegion(Region.US_EAST2)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second function app with the same app service plan

            System.out.println("Creating another function app " + app2Name + "...");
            FunctionApp app2 = azureResourceManager.functionApps().define(app2Name)
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Created function app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Purchase a domain (will be canceled for a full refund)

            System.out.println("Purchasing a domain " + domainName + "...");

            AppServiceDomain domain = azureResourceManager.appServiceDomains().define(domainName)
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
            // Bind domain to function app 1

            System.out.println("Binding http://" + app1Name + "." + domainName + " to function app " + app1Name + "...");

            app1 = app1.update()
                    .defineHostnameBinding()
                        .withAzureManagedDomain(domain)
                        .withSubDomain(app1Name)
                        .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
                        .attach()
                    .apply();

            System.out.println("Finished binding http://" + app1Name + "." + domainName + " to function app " + app1Name);
            Utils.print(app1);

            //============================================================
            // Create a self-singed SSL certificate

            String pfxPath = ManageFunctionAppWithDomainSsl.class.getResource("/").getPath() + "webapp_" + domainName + ".pfx";
            String cerPath = ManageFunctionAppWithDomainSsl.class.getResource("/").getPath() + "webapp_" + domainName + ".cer";

            System.out.println("Creating a self-signed certificate " + pfxPath + "...");

            Utils.createCertificate(cerPath, pfxPath, domainName, certPassword, "*." + domainName, null);

            System.out.println("Created self-signed certificate " + pfxPath);

            //============================================================
            // Bind domain to function app 2 and turn on wild card SSL for both

            System.out.println("Binding https://" + app1Name + "." + domainName + " to function app " + app1Name + "...");

            app1 = app1.update()
                    .withManagedHostnameBindings(domain, app1Name)
                    .defineSslBinding()
                        .forHostname(app1Name + "." + domainName)
                        .withPfxCertificateToUpload(new File(pfxPath), certPassword)
                        .withSniBasedSsl()
                        .attach()
                    .apply();

            System.out.println("Finished binding http://" + app1Name + "." + domainName + " to function app " + app1Name);
            Utils.print(app1);

            System.out.println("Binding https://" + app2Name + "." + domainName + " to function app " + app2Name + "...");

            app2 = app2.update()
                    .withManagedHostnameBindings(domain, app2Name)
                    .defineSslBinding()
                        .forHostname(app2Name + "." + domainName)
                        .withPfxCertificateToUpload(new File(pfxPath), certPassword)
                        .withSniBasedSsl()
                        .attach()
                    .apply();

            System.out.println("Finished binding http://" + app2Name + "." + domainName + " to function app " + app2Name);
            Utils.print(app2);

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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
}
