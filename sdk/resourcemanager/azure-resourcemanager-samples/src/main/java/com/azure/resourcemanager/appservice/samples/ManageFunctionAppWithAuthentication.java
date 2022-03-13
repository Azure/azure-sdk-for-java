// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.NameValuePair;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.time.Duration;


/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 3 function apps under the same new app service plan and with the same storage account
 *    - Deploy 1 &amp; 2 via Git a function that calculates the square of a number
 *    - Deploy 3 via Web Deploy
 *    - Enable app level authentication for the 1st function app
 *    - Verify the 1st function app can be accessed with the admin key
 *    - Enable function level authentication for the 2nd function app
 *    - Verify the 2nd function app can be accessed with the function key
 *    - Enable function level authentication for the 3rd function app
 *    - Verify the 3rd function app can be accessed with the function key
 */
public final class ManageFunctionAppWithAuthentication {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws GitAPIException {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name       = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String app3Name       = Utils.randomResourceName(azureResourceManager, "webapp3-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with admin level auth

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + " with admin level auth...");

            FunctionApp app1 = azureResourceManager.functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withLocalGitSourceControl()
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second function app with function level auth

            System.out.println("Creating another function app " + app2Name + " in resource group " + rgName + " with function level auth...");
            AppServicePlan plan = azureResourceManager.appServicePlans().getById(app1.appServicePlanId());
            FunctionApp app2 = azureResourceManager.functionApps().define(app2Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withExistingStorageAccount(app1.storageAccount())
                    .withLocalGitSourceControl()
                    .create();

            System.out.println("Created function app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Create a thrid function app with function level auth

            System.out.println("Creating another function app " + app3Name + " in resource group " + rgName + " with function level auth...");
            FunctionApp app3 = azureResourceManager.functionApps().define(app3Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withExistingStorageAccount(app1.storageAccount())
                    .withLocalGitSourceControl()
                    .create();

            System.out.println("Created function app " + app3.name());
            Utils.print(app3);

            //============================================================
            // Deploy to app 1 through Git

            System.out.println("Deploying a local function app to " + app1Name + " through Git...");

            PublishingProfile profile = app1.getPublishingProfile();
            Git git = Git
                    .init()
                    .setDirectory(new File(ManageFunctionAppWithAuthentication.class.getResource("/square-function-app-admin-auth/").getPath()))
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            PushCommand command = git.push();
            command.setRemote(profile.gitUrl());
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(profile.gitUsername(), profile.gitPassword()));
            command.setRefSpecs(new RefSpec("master:master"));
            command.setForce(true);
            command.call();

            System.out.println("Deployment to function app " + app1.name() + " completed");
            Utils.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/api/square...");
            Utils.sendPostRequest("http://" + app1Url + "/api/square", "625");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app1Url + "/api/square...");
            System.out.println("Square of 625 is " + Utils.sendPostRequest("http://" + app1Url + "/api/square?code=" + app1.getMasterKey(), "625"));

            //============================================================
            // Deploy to app 2 through Git

            System.out.println("Deploying a local function app to " + app2Name + " through Git...");

            profile = app2.getPublishingProfile();
            git = Git
                    .init()
                    .setDirectory(new File(ManageFunctionAppWithAuthentication.class.getResource("/square-function-app-function-auth/").getPath()))
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            command = git.push();
            command.setRemote(profile.gitUrl());
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(profile.gitUsername(), profile.gitPassword()));
            command.setRefSpecs(new RefSpec("master:master"));
            command.setForce(true);
            command.call();

            System.out.println("Deployment to function app " + app2.name() + " completed");
            Utils.print(app2);


            String functionKey = app2.listFunctionKeys("square").values().iterator().next();

            // warm up
            System.out.println("Warming up " + app2Url + "/api/square...");
            Utils.sendPostRequest("http://" + app2Url + "/api/square", "725");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app2Url + "/api/square...");
            System.out.println("Square of 725 is " + Utils.sendPostRequest("http://" + app2Url + "/api/square?code=" + functionKey, "725"));

            System.out.println("Adding a new key to function app " + app2.name() + "...");

            NameValuePair newKey = app2.addFunctionKey("square", "newkey", null);

            System.out.println("CURLing " + app2Url + "/api/square...");
            System.out.println("Square of 825 is " + Utils.sendPostRequest("http://" + app2Url + "/api/square?code=" + newKey.value(), "825"));

            //============================================================
            // Deploy to app 3 through Git

            System.out.println("Deploying a local function app to " + app3Name + " through web deploy...");

            app3.deploy()
                    .withPackageUri("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-samples/src/main/resources/square-function-app-function-auth.zip")
                    .withExistingDeploymentsDeleted(false)
                    .execute();

            System.out.println("Deployment to function app " + app3.name() + " completed");
            Utils.print(app3);

            System.out.println("Adding a new key to function app " + app3.name() + "...");

            app3.addFunctionKey("square", "newkey", "mysecretkey");

            // warm up
            System.out.println("Warming up " + app3Url + "/api/square...");
            Utils.sendPostRequest("http://" + app3Url + "/api/square", "925");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app3Url + "/api/square...");
            System.out.println("Square of 925 is " + Utils.sendPostRequest("http://" + app3Url + "/api/square?code=mysecretkey", "925"));

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
