// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.time.Duration;


/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 5 function apps under the same new app service plan:
 *    - Deploy to 1 using FTP
 *    - Deploy to 2 using local Git repository
 *    - Deploy to 3 using a publicly available Git repository
 *    - Deploy to 4 using a GitHub repository with continuous integration
 *    - Deploy to 5 using web deploy
 *    - Deploy to 6 using zip deploy
 */
public final class ManageFunctionAppSourceControl {

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
        final String app4Name       = Utils.randomResourceName(azureResourceManager, "webapp4-", 20);
        final String app5Name       = Utils.randomResourceName(azureResourceManager, "webapp5-", 20);
        final String app6Name       = Utils.randomResourceName(azureResourceManager, "webapp6-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String app4Url        = app4Name + suffix;
        final String app5Url        = app5Name + suffix;
        final String app6Url        = app6Name + suffix;
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with a new app service plan

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + "...");

            FunctionApp app1 = azureResourceManager.functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Deploy to app 1 through FTP

            System.out.println("Deploying a function app to " + app1Name + " through FTP...");

            Utils.uploadFileForFunctionViaFtp(app1.getPublishingProfile(), "host.json", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/host.json"));
            Utils.uploadFileForFunctionViaFtp(app1.getPublishingProfile(), "square/function.json", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/square/function.json"));
            Utils.uploadFileForFunctionViaFtp(app1.getPublishingProfile(), "square/index.js", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/square/index.js"));

            // sync triggers
            app1.syncTriggers();

            System.out.println("Deployment square app to function app " + app1.name() + " completed");
            Utils.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/api/square...");
            Utils.sendPostRequest("http://" + app1Url + "/api/square", "625");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app1Url + "/api/square...");
            System.out.println("Square of 625 is " + Utils.sendPostRequest("http://" + app1Url + "/api/square", "625"));

            //============================================================
            // Create a second function app with local git source control

            System.out.println("Creating another function app " + app2Name + " in resource group " + rgName + "...");
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
            // Deploy to app 2 through local Git

            System.out.println("Deploying a local Tomcat source to " + app2Name + " through Git...");

            PublishingProfile profile = app2.getPublishingProfile();
            Git git = Git
                    .init()
                    .setDirectory(new File(ManageFunctionAppSourceControl.class.getResource("/square-function-app/").getPath()))
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            PushCommand command = git.push();
            command.setRemote(profile.gitUrl());
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(profile.gitUsername(), profile.gitPassword()));
            command.setRefSpecs(new RefSpec("master:master"));
            command.setForce(true);
            command.call();

            System.out.println("Deployment to function app " + app2.name() + " completed");
            Utils.print(app2);

            // warm up
            System.out.println("Warming up " + app2Url + "/api/square...");
            Utils.sendPostRequest("http://" + app2Url + "/api/square", "725");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app2Url + "/api/square...");
            System.out.println("Square of 725 is " + Utils.sendPostRequest("http://" + app2Url + "/api/square", "725"));

            //============================================================
            // Create a 3rd function app with a public GitHub repo in Azure-Samples

            System.out.println("Creating another function app " + app3Name + "...");
            FunctionApp app3 = azureResourceManager.functionApps().define(app3Name)
                    .withExistingAppServicePlan(plan)
                    .withNewResourceGroup(rgName)
                    .withExistingStorageAccount(app2.storageAccount())
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/jianghaolu/square-function-app-sample")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created function app " + app3.name());
            Utils.print(app3);

            // warm up
            System.out.println("Warming up " + app3Url + "/api/square...");
            Utils.sendPostRequest("http://" + app3Url + "/api/square", "825");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app3Url + "/api/square...");
            System.out.println("Square of 825 is " + Utils.sendPostRequest("http://" + app3Url + "/api/square", "825"));

            //============================================================
            // Create a 4th function app with a personal GitHub repo and turn on continuous integration

            System.out.println("Creating another function app " + app4Name + "...");
            FunctionApp app4 = azureResourceManager.functionApps()
                    .define(app4Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withExistingStorageAccount(app3.storageAccount())
                    // Uncomment the following lines to turn on 4th scenario
                    //.defineSourceControl()
                    //    .withContinuouslyIntegratedGitHubRepository("username", "reponame")
                    //    .withBranch("master")
                    //    .withGitHubAccessToken("YOUR GITHUB PERSONAL TOKEN")
                    //    .attach()
                    .create();

            System.out.println("Created function app " + app4.name());
            Utils.print(app4);

            // warm up
            System.out.println("Warming up " + app4Url + "...");
            Utils.sendGetRequest("http://" + app4Url);
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app4Url + "...");
            System.out.println(Utils.sendGetRequest("http://" + app4Url));

            //============================================================
            // Create a 5th function app with web deploy

            System.out.println("Creating another function app " + app5Name + "...");
            FunctionApp app5 = azureResourceManager.functionApps()
                    .define(app5Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withExistingStorageAccount(app3.storageAccount())
                    .create();

            System.out.println("Created function app " + app5.name());

            System.out.println("Deploy to " + app5Name + " through web deploy...");
            app5.deploy()
                    .withPackageUri("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-appservice/src/test/resources/webapps.zip")
                    .withExistingDeploymentsDeleted(true)
                    .execute();

            // warm up
            System.out.println("Warming up " + app5Url + "/api/square...");
            Utils.sendPostRequest("http://" + app5Url + "/api/square", "925");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app5Url + "/api/square...");
            System.out.println("Square of 925 is " + Utils.sendPostRequest("http://" + app5Url + "/api/square", "925"));

            //============================================================
            // Create a 6th function app with zip deploy

            System.out.println("Creating another function app " + app6Name + "...");
            FunctionApp app6 = azureResourceManager.functionApps()
                    .define(app6Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .create();
//
            System.out.println("Created function app " + app6.name());

            //============================================================
            // Deploy to the 6th function app through ZIP deploy

            System.out.println("Deploying square-function-app.zip to " + app6Name + " through ZIP deploy...");

            app6.zipDeploy(new File(ManageFunctionAppSourceControl.class.getResource("/square-function-app.zip").getPath()));

            // warm up
            System.out.println("Warming up " + app6Url + "/api/square...");
            Utils.sendPostRequest("http://" + app6Url + "/api/square", "926");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app6Url + "/api/square...");
            System.out.println("Square of 926 is " + Utils.sendPostRequest("http://" + app6Url + "/api/square", "926"));

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
