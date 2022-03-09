// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.core.management.Region;

import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficRoutingMethod;

import java.io.File;
import java.io.IOException;

/**
 * Azure App Service sample for managing web apps.
 *  - Create a domain
 *  - Create a self-signed certificate for the domain
 *  - Create 3 app service plans in 3 different regions
 *  - Create 5 web apps under the 3 plans, bound to the domain and the certificate
 *  - Create a traffic manager in front of the web apps
 *  - Scale up the app service plans to twice the capacity
 */
public final class ManageWebAppWithTrafficManager {
    private static String rgName;
    // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
    private static final String CERT_PASSWORD = "StrongPass!12";

    private static AzureResourceManager azureResourceManager;
    private static AppServiceDomain domain;
    private static String pfxPath;

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV_", 24);

        if (ManageWebAppWithTrafficManager.azureResourceManager == null) {
            ManageWebAppWithTrafficManager.azureResourceManager = azureResourceManager;
        }

        // New resources
        final String app1Name = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String app3Name = Utils.randomResourceName(azureResourceManager, "webapp3-", 20);
        final String app4Name = Utils.randomResourceName(azureResourceManager, "webapp4-", 20);
        final String app5Name = Utils.randomResourceName(azureResourceManager, "webapp5-", 20);
        final String plan1Name = Utils.randomResourceName(azureResourceManager, "jplan1_", 15);
        final String plan2Name = Utils.randomResourceName(azureResourceManager, "jplan2_", 15);
        final String plan3Name = Utils.randomResourceName(azureResourceManager, "jplan3_", 15);
        final String domainName = Utils.randomResourceName(azureResourceManager, "jsdkdemo-", 20) + ".com";
        final String tmName = Utils.randomResourceName(azureResourceManager, "jsdktm-", 20);

        try {

            //============================================================
            // Purchase a domain (will be canceled for a full refund)

            System.out.println("Purchasing a domain " + domainName + "...");

            azureResourceManager.resourceGroups().define(rgName)
                .withRegion(Region.US_WEST)
                .create();

            domain = azureResourceManager.appServiceDomains().define(domainName)
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
            // Create a self-singed SSL certificate

            pfxPath = ManageWebAppWithTrafficManager.class.getResource("/").getPath() + "webapp_" + domainName + ".pfx";
            String cerPath = ManageWebAppWithTrafficManager.class.getResource("/").getPath() + "webapp_" + domainName + ".cer";

            System.out.println("Creating a self-signed certificate " + pfxPath + "...");

            Utils.createCertificate(cerPath, pfxPath, domainName, CERT_PASSWORD, "*." + domainName, null);

            //============================================================
            // Create 3 app service plans in 3 regions

            System.out.println("Creating app service plan " + plan1Name + " in US West...");

            AppServicePlan plan1 = createAppServicePlan(plan1Name, Region.US_WEST);

            System.out.println("Created app service plan " + plan1.name());
            Utils.print(plan1);

            System.out.println("Creating app service plan " + plan2Name + " in Europe West...");

            AppServicePlan plan2 = createAppServicePlan(plan2Name, Region.EUROPE_WEST);

            System.out.println("Created app service plan " + plan2.name());
            Utils.print(plan1);

            System.out.println("Creating app service plan " + plan3Name + " in Asia East...");

            AppServicePlan plan3 = createAppServicePlan(plan3Name, Region.ASIA_EAST);

            System.out.println("Created app service plan " + plan2.name());
            Utils.print(plan1);

            //============================================================
            // Create 5 web apps under these 3 app service plans

            System.out.println("Creating web app " + app1Name + "...");

            WebApp app1 = createWebApp(app1Name, plan1);

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            System.out.println("Creating another web app " + app2Name + "...");
            WebApp app2 = createWebApp(app2Name, plan2);

            System.out.println("Created web app " + app2.name());
            Utils.print(app2);

            System.out.println("Creating another web app " + app3Name + "...");
            WebApp app3 = createWebApp(app3Name, plan3);

            System.out.println("Created web app " + app3.name());
            Utils.print(app3);

            System.out.println("Creating another web app " + app3Name + "...");
            WebApp app4 = createWebApp(app4Name, plan1);

            System.out.println("Created web app " + app4.name());
            Utils.print(app4);

            System.out.println("Creating another web app " + app3Name + "...");
            WebApp app5 = createWebApp(app5Name, plan1);

            System.out.println("Created web app " + app5.name());
            Utils.print(app5);

            //============================================================
            // Create a traffic manager

            System.out.println("Creating a traffic manager " + tmName + " for the web apps...");

            TrafficManagerProfile trafficManager = azureResourceManager.trafficManagerProfiles().define(tmName)
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

            System.out.println("Created traffic manager " + trafficManager.name());
            Utils.print(trafficManager);

            //============================================================
            // Scale up the app service plans

            System.out.println("Scaling up app service plan " + plan1Name + "...");

            plan1.update()
                .withCapacity(plan1.capacity() * 2)
                .apply();

            System.out.println("Scaled up app service plan " + plan1Name);
            Utils.print(plan1);

            System.out.println("Scaling up app service plan " + plan2Name + "...");

            plan2.update()
                .withCapacity(plan2.capacity() * 2)
                .apply();

            System.out.println("Scaled up app service plan " + plan2Name);
            Utils.print(plan2);

            System.out.println("Scaling up app service plan " + plan3Name + "...");

            plan3.update()
                .withCapacity(plan3.capacity() * 2)
                .apply();

            System.out.println("Scaled up app service plan " + plan3Name);
            Utils.print(plan3);

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

    private static AppServicePlan createAppServicePlan(String name, Region region) {
        return azureResourceManager.appServicePlans().define(name)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withPricingTier(PricingTier.STANDARD_S1)
                .withOperatingSystem(OperatingSystem.WINDOWS)
                .create();
    }

    private static WebApp createWebApp(String name, AppServicePlan plan) {
        return azureResourceManager.webApps().define(name)
                .withExistingWindowsPlan(plan)
                .withExistingResourceGroup(rgName)
                .withManagedHostnameBindings(domain, name)
                .defineSslBinding()
                    .forHostname(name + "." + domain.name())
                    .withPfxCertificateToUpload(new File(pfxPath), CERT_PASSWORD)
                    .withSniBasedSsl()
                    .attach()
                .defineSourceControl()
                    .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
                    .withBranch("master")
                    .attach()
                .create();
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate
            //
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
