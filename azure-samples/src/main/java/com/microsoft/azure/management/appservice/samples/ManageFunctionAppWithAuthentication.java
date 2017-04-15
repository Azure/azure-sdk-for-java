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
import okhttp3.Headers;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 4 function apps under the same new app service plan:
 *    - Deploy to 1 using FTP
 *    - Deploy to 2 using local Git repository
 *    - Deploy to 3 using a publicly available Git repository
 *    - Deploy to 4 using a GitHub repository with continuous integration
 */
public final class ManageFunctionAppWithAuthentication {

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
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String rgName         = SdkContext.randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with admin level auth

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + " with admin level auth...");

            FunctionApp app1 = azure.appServices().functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withLocalGitSourceControl()
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            //============================================================
            // Create a second function app with function level auth

            System.out.println("Creating another function app " + app2Name + " in resource group " + rgName + " with function level auth...");
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
            post("http://" + app1Url + "/api/square", "625");
            Thread.sleep(5000);
            System.out.println("CURLing " + app1Url + "/api/square...");
            System.out.println("Square of 625 is " + post("http://" + app1Url + "/api/square?code=" + app1.getMasterKey(), "625"));

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


            String masterKey = app2.getMasterKey();
            Map<String, String> functionsHeader = new HashMap<>();
            functionsHeader.put("x-functions-key", masterKey);
            String response = curl("http://" + app2Url + "/admin/functions/square/keys", functionsHeader);
            Pattern pattern = Pattern.compile("\"name\":\"default\",\"value\":\"([\\w=/]+)\"");
            Matcher matcher = pattern.matcher(response);
            matcher.find();
            String functionKey = matcher.group(1);

            // warm up
            System.out.println("Warming up " + app2Url + "/api/square...");
            post("http://" + app2Url + "/api/square", "725");
            Thread.sleep(5000);
            System.out.println("CURLing " + app2Url + "/api/square...");
            System.out.println("Square of 725 is " + post("http://" + app2Url + "/api/square?code=" + functionKey, "725"));

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

    private static String curl(String url, Map<String, String> headers) {
        Request request = new Request.Builder().url(url).headers(Headers.of(headers)).get().build();
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
