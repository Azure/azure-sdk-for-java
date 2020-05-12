// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
//package com.azure.management.appservice.samples;
//
//import com.azure.core.credential.TokenCredential;
//import com.azure.core.http.policy.HttpLogDetailLevel;
//import com.azure.core.management.AzureEnvironment;
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import com.azure.management.Azure;
//import com.azure.management.resources.fluentcore.profile.AzureProfile;
//
//import com.azure.management.trafficmanager.TrafficManagerProfile;
//import com.azure.management.trafficmanager.TrafficRoutingMethod;
//
//
///**
// * Azure App Service sample for managing web apps.
// *  - Create a domain
// *  - Create a self-signed certificate for the domain
// *  - Create 3 app service plans in 3 different regions
// *  - Create 5 web apps under the 3 plans, bound to the domain and the certificate
// *  - Create a traffic manager in front of the web apps
// *  - Scale up the app service plans to twice the capacity
// */
//public final class ManageLinuxWebAppWithTrafficManager {
//    private static String rgName;
//    [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
//    private static final String CERT_PASSWORD = "StrongPass!12";
//
//    private static Azure azure;
//    private static AppServiceDomain domain;
//    private static String pfxPath;

//    /**
//     * Main function which runs the actual sample.
//     * @param azure instance of the azure client
//     * @return true if sample runs successfully
//     */
//    public static boolean runSample(Azure azure) {
//        System.out.println("removed later");
//        return true;
//        RG_NAME = azure.sdkContext().randomResourceName("rgNEMV_", 24);
//
//        if (ManageLinuxWebAppWithTrafficManager.azure == null) {
//            ManageLinuxWebAppWithTrafficManager.azure = azure;
//        }
//
//        // New resources
//        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
//        final String app2Name       = azure.sdkContext().randomResourceName("webapp2-", 20);
//        final String app3Name       = azure.sdkContext().randomResourceName("webapp3-", 20);
//        final String app4Name       = azure.sdkContext().randomResourceName("webapp4-", 20);
//        final String app5Name       = azure.sdkContext().randomResourceName("webapp5-", 20);
//        final String plan1Name      = azure.sdkContext().randomResourceName("jplan1_", 15);
//        final String plan2Name      = azure.sdkContext().randomResourceName("jplan2_", 15);
//        final String plan3Name      = azure.sdkContext().randomResourceName("jplan3_", 15);
//        final String domainName     = azure.sdkContext().randomResourceName("jsdkdemo-", 20) + ".com";
//        final String tmName         = azure.sdkContext().randomResourceName("jsdktm-", 20);
//
//        try {
//
//            //============================================================
//            // Purchase a domain (will be canceled for a full refund)
//
//            System.out.println("Purchasing a domain " + domainName + "...");
//
//            azure.resourceGroups().define(RG_NAME)
//                    .withRegion(Region.US_WEST)
//                    .create();
//
//            domain = azure.appServices().domains().define(domainName)
//                    .withExistingResourceGroup(RG_NAME)
//                    .defineRegistrantContact()
//                        .withFirstName("Jon")
//                        .withLastName("Doe")
//                        .withEmail("jondoe@contoso.com")
//                        .withAddressLine1("123 4th Ave")
//                        .withCity("Redmond")
//                        .withStateOrProvince("WA")
//                        .withCountry(CountryIsoCode.UNITED_STATES)
//                        .withPostalCode("98052")
//                        .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
//                        .withPhoneNumber("4258828080")
//                        .attach()
//                    .withDomainPrivacyEnabled(true)
//                    .withAutoRenewEnabled(false)
//                    .create();
//            System.out.println("Purchased domain " + domain.name());
//            Utils.print(domain);
//
//            //============================================================
//            // Create a self-singed SSL certificate
//
//            pfxPath = ManageLinuxWebAppWithTrafficManager.class.getResource("/").getPath() + "webapp_" + ManageLinuxWebAppWithTrafficManager.class.getSimpleName().toLowerCase() + ".pfx";
//            String cerPath = ManageLinuxWebAppWithTrafficManager.class.getResource("/").getPath() + "webapp_" + ManageLinuxWebAppWithTrafficManager.class.getSimpleName().toLowerCase() + ".cer";
//
//            System.out.println("Creating a self-signed certificate " + pfxPath + "...");
//
//            Utils.createCertificate(cerPath, pfxPath, domainName, CERT_PASSWORD, "*." + domainName);
//
//            //============================================================
//            // Create 3 app service plans in 3 regions
//
//            System.out.println("Creating app service plan " + plan1Name + " in US West...");
//
//            AppServicePlan plan1 = createAppServicePlan(plan1Name, Region.US_WEST);
//
//            System.out.println("Created app service plan " + plan1.name());
//            Utils.print(plan1);
//
//            System.out.println("Creating app service plan " + plan2Name + " in Europe West...");
//
//            AppServicePlan plan2 = createAppServicePlan(plan2Name, Region.EUROPE_WEST);
//
//            System.out.println("Created app service plan " + plan2.name());
//            Utils.print(plan1);
//
//            System.out.println("Creating app service plan " + plan3Name + " in Asia East...");
//
//            AppServicePlan plan3 = createAppServicePlan(plan3Name, Region.ASIA_SOUTHEAST);
//
//            System.out.println("Created app service plan " + plan2.name());
//            Utils.print(plan1);
//
//            //============================================================
//            // Create 5 web apps under these 3 app service plans
//
//            System.out.println("Creating web app " + app1Name + "...");
//
//            WebApp app1 = createWebApp(app1Name, plan1);
//
//            System.out.println("Created web app " + app1.name());
//            Utils.print(app1);
//
//            System.out.println("Creating another web app " + app2Name + "...");
//            WebApp app2 = createWebApp(app2Name, plan2);
//
//            System.out.println("Created web app " + app2.name());
//            Utils.print(app2);
//
//            System.out.println("Creating another web app " + app3Name + "...");
//            WebApp app3 = createWebApp(app3Name, plan3);
//
//            System.out.println("Created web app " + app3.name());
//            Utils.print(app3);
//
//            System.out.println("Creating another web app " + app3Name + "...");
//            WebApp app4 = createWebApp(app4Name, plan1);
//
//            System.out.println("Created web app " + app4.name());
//            Utils.print(app4);
//
//            System.out.println("Creating another web app " + app3Name + "...");
//            WebApp app5 = createWebApp(app5Name, plan1);
//
//            System.out.println("Created web app " + app5.name());
//            Utils.print(app5);
//
//            //============================================================
//            // Create a traffic manager
//
//            System.out.println("Creating a traffic manager " + tmName + " for the web apps...");
//
//            TrafficManagerProfile trafficManager = azure.trafficManagerProfiles().define(tmName)
//                    .withExistingResourceGroup(RG_NAME)
//                    .withLeafDomainLabel(tmName)
//                    .withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY)
//                    .defineAzureTargetEndpoint("endpoint1")
//                        .toResourceId(app1.id())
//                        .withRoutingPriority(1)
//                        .attach()
//                    .defineAzureTargetEndpoint("endpoint2")
//                        .toResourceId(app2.id())
//                        .withRoutingPriority(2)
//                        .attach()
//                    .defineAzureTargetEndpoint("endpoint3")
//                        .toResourceId(app3.id())
//                        .withRoutingPriority(3)
//                        .attach()
//                    .create();
//
//            System.out.println("Created traffic manager " + trafficManager.name());
//            Utils.print(trafficManager);
//
//            //============================================================
//            // Scale up the app service plans
//
//            System.out.println("Scaling up app service plan " + plan1Name + "...");
//
//            plan1.update()
//                    .withCapacity(plan1.capacity() * 2)
//                    .apply();
//
//            System.out.println("Scaled up app service plan " + plan1Name);
//            Utils.print(plan1);
//
//            System.out.println("Scaling up app service plan " + plan2Name + "...");
//
//            plan2.update()
//                    .withCapacity(plan2.capacity() * 2)
//                    .apply();
//
//            System.out.println("Scaled up app service plan " + plan2Name);
//            Utils.print(plan2);
//
//            System.out.println("Scaling up app service plan " + plan3Name + "...");
//
//            plan3.update()
//                    .withCapacity(plan3.capacity() * 2)
//                    .apply();
//
//            System.out.println("Scaled up app service plan " + plan3Name);
//            Utils.print(plan3);
//
//            return true;
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try {
//                System.out.println("Deleting Resource Group: " + RG_NAME);
//                azure.resourceGroups().beginDeleteByName(RG_NAME);
//                System.out.println("Deleted Resource Group: " + RG_NAME);
//            } catch (NullPointerException npe) {
//                System.out.println("Did not create any resources in Azure. No clean up is necessary");
//            } catch (Exception g) {
//                g.printStackTrace();
//            }
//        }
//
//        return false;
//
//    private static AppServicePlan createAppServicePlan(String name, Region region) {
//        return azure.appServicePlans().define(name)
//                .withRegion(region)
//                .withExistingResourceGroup(rgName)
//                .withPricingTier(PricingTier.STANDARD_S2)
//                .withOperatingSystem(OperatingSystem.LINUX)
//                .create();
//    }
//
//    private static WebApp createWebApp(String name, AppServicePlan plan) {
//        return azure.webApps().define(name)
//                .withExistingLinuxPlan(plan)
//                .withExistingResourceGroup(rgName)
//                .withBuiltInImage(RuntimeStack.NODEJS_4_5)
//                .withManagedHostnameBindings(domain, name)
//                .defineSslBinding()
//                    .forHostname(name + "." + domain.name())
//                    .withPfxCertificateToUpload(new File(pfxPath), CERT_PASSWORD)
//                    .withSniBasedSsl()
//                    .attach()
//                .defineSourceControl()
//                    .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
//                    .withBranch("master")
//                    .attach()
//                .create();
//    }
//}
