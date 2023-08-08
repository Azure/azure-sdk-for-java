// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.BuiltInAuthenticationProvider;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;

import java.io.Console;

/**
 * Azure App Service sample for managing authentication for web apps.
 *  - Create 4 web apps under the same new app service plan with:
 *    - Active Directory login for 1
 *    - Facebook login for 2
 *    - Google login for 3
 *    - Microsoft login for 4
 */
public final class ManageWebAppWithAuthentication {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name       = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String app3Name       = Utils.randomResourceName(azureResourceManager, "webapp3-", 20);
        final String app4Name       = Utils.randomResourceName(azureResourceManager, "webapp4-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String app4Url        = app4Name + suffix;
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {


            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + app1Name + " in resource group " + rgName + "...");

            WebApp app1 = azureResourceManager.webApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                    .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Set up active directory authentication

            System.out.println("Please create an AD application with redirect URL " + app1Url);
            System.out.print("Application ID is:");
            Console console = System.console();
            String applicationId = console.readLine();
            System.out.print("Tenant ID is:");
            String tenantId = console.readLine();

            System.out.println("Updating web app " + app1Name + " to use active directory login...");

            app1.update()
                    .defineAuthentication()
                        .withDefaultAuthenticationProvider(BuiltInAuthenticationProvider.AZURE_ACTIVE_DIRECTORY)
                        .withActiveDirectory(applicationId, "https://sts.windows.net/" + tenantId)
                        .attach()
                    .apply();

            System.out.println("Added active directory login to " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second web app

            System.out.println("Creating another web app " + app2Name + " in resource group " + rgName + "...");
            AppServicePlan plan = azureResourceManager.appServicePlans().getById(app1.appServicePlanId());
            WebApp app2 = azureResourceManager.webApps().define(app2Name)
                    .withExistingWindowsPlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                    .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                    .create();

            System.out.println("Created web app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Set up Facebook authentication

            System.out.println("Please create a Facebook developer application with whitelisted URL " + app2Url);
            System.out.print("App ID is:");
            String fbAppId = console.readLine();
            System.out.print("App secret is:");
            String fbAppSecret = console.readLine();

            System.out.println("Updating web app " + app2Name + " to use Facebook login...");

            app2.update()
                    .defineAuthentication()
                        .withDefaultAuthenticationProvider(BuiltInAuthenticationProvider.FACEBOOK)
                        .withFacebook(fbAppId, fbAppSecret)
                        .attach()
                    .apply();

            System.out.println("Added Facebook login to " + app2.name());
            Utils.print(app2);

            //============================================================
            // Create a 3rd web app with a public GitHub repo in Azure-Samples

            System.out.println("Creating another web app " + app3Name + "...");
            WebApp app3 = azureResourceManager.webApps().define(app3Name)
                    .withExistingWindowsPlan(plan)
                    .withNewResourceGroup(rgName)
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/Azure-Samples/app-service-web-dotnet-get-started")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created web app " + app3.name());
            Utils.print(app3);

            //============================================================
            // Set up Google authentication

            System.out.println("Please create a Google developer application with redirect URL " + app3Url);
            System.out.print("Client ID is:");
            String gClientId = console.readLine();
            System.out.print("Client secret is:");
            String gClientSecret = console.readLine();

            System.out.println("Updating web app " + app3Name + " to use Google login...");

            app3.update()
                    .defineAuthentication()
                        .withDefaultAuthenticationProvider(BuiltInAuthenticationProvider.GOOGLE)
                        .withGoogle(gClientId, gClientSecret)
                        .attach()
                    .apply();

            System.out.println("Added Google login to " + app3.name());
            Utils.print(app3);

            //============================================================
            // Create a 4th web app

            System.out.println("Creating another web app " + app4Name + "...");
            WebApp app4 = azureResourceManager.webApps()
                    .define(app4Name)
                    .withExistingWindowsPlan(plan)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Created web app " + app4.name());
            Utils.print(app4);

            //============================================================
            // Set up Google authentication

            System.out.println("Please create a Microsoft developer application with redirect URL " + app4Url);
            System.out.print("Client ID is:");
            String clientId = console.readLine();
            System.out.print("Client secret is:");
            String clientSecret = console.readLine();

            System.out.println("Updating web app " + app3Name + " to use Microsoft login...");

            app4.update()
                    .defineAuthentication()
                        .withDefaultAuthenticationProvider(BuiltInAuthenticationProvider.MICROSOFT_ACCOUNT)
                        .withMicrosoft(clientId, clientSecret)
                        .attach()
                    .apply();

            System.out.println("Added Microsoft login to " + app4.name());
            Utils.print(app4);

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
