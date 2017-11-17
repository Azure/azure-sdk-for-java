/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 5 function apps under the same new app service plan:
 *    - Deploy to 1 using FTP
 *    - Deploy to 2 using local Git repository
 *    - Deploy to 3 using a publicly available Git repository
 *    - Deploy to 4 using a GitHub repository with continuous integration
 *    - Deploy to 5 using web deploy
 */
public final class ManageFunctionAppSourceControl {

    private static OkHttpClient httpClient;

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = SdkContext.randomResourceName("webapp1-", 20);
        final String app2Name       = SdkContext.randomResourceName("webapp2-", 20);
        final String app3Name       = SdkContext.randomResourceName("webapp3-", 20);
        final String app4Name       = SdkContext.randomResourceName("webapp4-", 20);
        final String app5Name       = SdkContext.randomResourceName("webapp5-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String app4Url        = app4Name + suffix;
        final String app5Url        = app5Name + suffix;
        final String rgName         = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with a new app service plan

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + "...");

            FunctionApp app1 = azure.appServices().functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Deploy to app 1 through FTP

            System.out.println("Deploying a function app to " + app1Name + " through FTP...");

            Utils.uploadFileToFunctionApp(app1.getPublishingProfile(), "host.json", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/host.json"));
            Utils.uploadFileToFunctionApp(app1.getPublishingProfile(), "square/function.json", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/square/function.json"));
            Utils.uploadFileToFunctionApp(app1.getPublishingProfile(), "square/index.js", ManageFunctionAppSourceControl.class.getResourceAsStream("/square-function-app/square/index.js"));

            // sync triggers
            app1.syncTriggers();

            System.out.println("Deployment square app to function app " + app1.name() + " completed");
            Utils.print(app1);

            // warm up
            System.out.println("Warming up " + app1Url + "/api/square...");
            post("http://" + app1Url + "/api/square", "625");
            Thread.sleep(5000);
            System.out.println("CURLing " + app1Url + "/api/square...");
            System.out.println("Square of 625 is " + post("http://" + app1Url + "/api/square", "625"));

            //============================================================
            // Create a second function app with local git source control

            System.out.println("Creating another function app " + app2Name + " in resource group " + rgName + "...");
            AppServicePlan plan = azure.appServices().appServicePlans().getById(app1.appServicePlanId());
            FunctionApp app2 = azure.appServices().functionApps().define(app2Name)
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
            post("http://" + app2Url + "/api/square", "725");
            Thread.sleep(5000);
            System.out.println("CURLing " + app2Url + "/api/square...");
            System.out.println("Square of 725 is " + post("http://" + app2Url + "/api/square", "725"));

            //============================================================
            // Create a 3rd function app with a public GitHub repo in Azure-Samples

            System.out.println("Creating another function app " + app3Name + "...");
            FunctionApp app3 = azure.appServices().functionApps().define(app3Name)
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
            post("http://" + app3Url + "/api/square", "825");
            Thread.sleep(5000);
            System.out.println("CURLing " + app3Url + "/api/square...");
            System.out.println("Square of 825 is " + post("http://" + app3Url + "/api/square", "825"));

            //============================================================
            // Create a 4th function app with a personal GitHub repo and turn on continuous integration

            System.out.println("Creating another function app " + app4Name + "...");
            FunctionApp app4 = azure.appServices().functionApps()
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
            curl("http://" + app4Url);
            Thread.sleep(5000);
            System.out.println("CURLing " + app4Url + "...");
            System.out.println(curl("http://" + app4Url));

            //============================================================
            // Create a 5th function app with web deploy

            System.out.println("Creating another function app " + app5Name + "...");
            FunctionApp app5 = azure.appServices().functionApps()
                    .define(app5Name)
                    .withExistingAppServicePlan(plan)
                    .withExistingResourceGroup(rgName)
                    .withExistingStorageAccount(app3.storageAccount())
                    .create();

            System.out.println("Created function app " + app5.name());

            System.out.println("Deploy to " + app5Name + " through web deploy...");
            app5.deploy()
                    .withPackageUri("https://github.com/Azure/azure-sdk-for-java/raw/master/azure-mgmt-appservice/src/test/resources/webapps.zip")
                    .withExistingDeploymentsDeleted(true)
                    .execute();

            // warm up
            System.out.println("Warming up " + app5Url + "/api/square...");
            post("http://" + app5Url + "/api/square", "925");
            Thread.sleep(5000);
            System.out.println("CURLing " + app5Url + "/api/square...");
            System.out.println("Square of 925 is " + post("http://" + app5Url + "/api/square", "925"));

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
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

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.BASIC)
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

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    private static String post(String url, String body) {
        Request request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("text/plain"), body)).build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }
}
