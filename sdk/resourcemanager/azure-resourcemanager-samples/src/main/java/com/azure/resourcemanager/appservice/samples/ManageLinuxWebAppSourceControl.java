// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
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
 * Azure App Service basic sample for managing web apps.
 *  - Create 4 web apps under the same new app service plan:
 *    - Deploy to 1 using FTP
 *    - Deploy to 2 using local Git repository
 *    - Deploy to 3 using a publicly available Git repository
 *    - Deploy to 4 using a GitHub repository with continuous integration
 */
public final class ManageLinuxWebAppSourceControl {

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
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JRE8)
                    .create();

            System.out.println("Created web app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Deploy to app 1 through FTP

            System.out.println("Deploying helloworld.war to " + app1Name + " through FTP...");

            Utils.uploadFileViaFtp(app1.getPublishingProfile(), "helloworld.war", ManageLinuxWebAppSourceControl.class.getResourceAsStream("/helloworld.war"));

            System.out.println("Deployment helloworld.war to web app " + app1.name() + " completed");
            Utils.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/helloworld...");
            Utils.sendGetRequest("http://" + app1Url + "/helloworld/");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app1Url + "/helloworld...");
            System.out.println(Utils.sendGetRequest("http://" + app1Url + "/helloworld/"));

            //============================================================
            // Create a second web app with local git source control

            System.out.println("Creating another web app " + app2Name + " in resource group " + rgName + "...");
            AppServicePlan plan = azureResourceManager.appServicePlans().getById(app1.appServicePlanId());
            WebApp app2 = azureResourceManager.webApps().define(app2Name)
                    .withExistingLinuxPlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JRE8)
                    .withLocalGitSourceControl()
                    .create();

            System.out.println("Created web app " + app2.name());
            Utils.print(app2);

            //============================================================
            // Deploy to app 2 through local Git

            System.out.println("Deploying a local Tomcat source to " + app2Name + " through Git...");

            PublishingProfile profile = app2.getPublishingProfile();
            Git git = Git
                    .init()
                    .setDirectory(new File(ManageLinuxWebAppSourceControl.class.getResource("/azure-samples-appservice-helloworld/").getPath()))
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            PushCommand command = git.push();
            command.setRemote(profile.gitUrl());
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(profile.gitUsername(), profile.gitPassword()));
            command.setRefSpecs(new RefSpec("master:master"));
            command.setForce(true);
            command.call();

            System.out.println("Deployment to web app " + app2.name() + " completed");
            Utils.print(app2);

            // warm up
            System.out.println("Warming up " + app2Url + "/helloworld...");
            Utils.sendGetRequest("http://" + app2Url + "/helloworld/");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app2Url + "/helloworld...");
            System.out.println(Utils.sendGetRequest("http://" + app2Url + "/helloworld/"));

            //============================================================
            // Create a 3rd web app with a public GitHub repo in Azure-Samples

            System.out.println("Creating another web app " + app3Name + "...");
            WebApp app3 = azureResourceManager.webApps().define(app3Name)
                    .withExistingLinuxPlan(plan)
                    .withNewResourceGroup(rgName)
                    .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JRE8)
                        .defineSourceControl()
                        .withPublicGitRepository("https://github.com/azure-appservice-samples/java-get-started")
                        .withBranch("master")
                        .attach()
                    .create();

            System.out.println("Created web app " + app3.name());
            Utils.print(app3);

            // warm up
            System.out.println("Warming up " + app3Url + "...");
            Utils.sendGetRequest("http://" + app3Url);
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app3Url + "...");
            System.out.println(Utils.sendGetRequest("http://" + app3Url));

            //============================================================
            // Create a 4th web app with a personal GitHub repo and turn on continuous integration

            System.out.println("Creating another web app " + app4Name + "...");
            WebApp app4 = azureResourceManager.webApps()
                    .define(app4Name)
                    .withExistingLinuxPlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withBuiltInImage(RuntimeStack.TOMCAT_8_5_JRE8)
                    // Uncomment the following lines to turn on 4th scenario
                    //.defineSourceControl()
                    //    .withContinuouslyIntegratedGitHubRepository("username", "reponame")
                    //    .withBranch("master")
                    //    .withGitHubAccessToken("YOUR GITHUB PERSONAL TOKEN")
                    //    .attach()
                    .create();

            System.out.println("Created web app " + app4.name());
            Utils.print(app4);

            // warm up
            System.out.println("Warming up " + app4Url + "...");
            Utils.sendGetRequest("http://" + app4Url);
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            System.out.println("CURLing " + app4Url + "...");
            System.out.println(Utils.sendGetRequest("http://" + app4Url));

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
