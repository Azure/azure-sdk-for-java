// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.core.http.policy.HttpLogDetailLevel;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import reactor.core.publisher.Flux;

import java.io.File;


/**
 * Azure App Service basic sample for managing web apps.
 *  - Create 4 web apps under the same new app service plan:
 *    - Deploy to 1 using FTP
 *    - Deploy to 2 using local Git repository
 *    - Deploy to 3 using a publicly available Git repository
 *    - Deploy to 4 using a GitHub repository with continuous integration
 */
public final class ManageWebAppSourceControlAsync {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String app2Name       = azure.sdkContext().randomResourceName("webapp2-", 20);
        final String app3Name       = azure.sdkContext().randomResourceName("webapp3-", 20);
        final String app4Name       = azure.sdkContext().randomResourceName("webapp4-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String app3Url        = app3Name + suffix;
        final String app4Url        = app4Name + suffix;
        final String planName       = azure.sdkContext().randomResourceName("jplan_", 15);
        final String rgName         = azure.sdkContext().randomResourceName("rg1NEMV_", 24);

        try {


            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + app1Name + " in resource group " + rgName + "...");

            Flux<?> app1Observable = azure.webApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.STANDARD_S1)
                    .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                    .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                    .createAsync()
                    .flatMap(indexable -> {
                        if (indexable instanceof WebApp) {
                            WebApp app = (WebApp) indexable;
                            System.out.println("Created web app " + app.name());
                            return Flux.merge(
                                    Flux.just(indexable),
                                    app.getPublishingProfileAsync()
                                    .map(publishingProfile -> {
                                        System.out.println("Deploying helloworld.war to " + app1Name + " through FTP...");
                                        Utils.uploadFileViaFtp(publishingProfile,
                                                "helloworld.war",
                                                ManageWebAppSourceControlAsync.class.getResourceAsStream("/helloworld.war"));

                                        System.out.println("Deployment helloworld.war to web app " + app1Name + " completed");
                                        return publishingProfile;
                                    }));
                        }
                        return Flux.just(indexable);
                    });

            System.out.println("Creating another web app " + app2Name + " in resource group " + rgName + "...");
            System.out.println("Creating another web app " + app3Name + "...");
            System.out.println("Creating another web app " + app4Name + "...");

            Flux<?> app234Observable = azure.appServicePlans()
                    .getByResourceGroupAsync(rgName, planName)
                    .flatMapMany(plan -> {
                        return Flux.merge(
                                azure.webApps().define(app2Name)
                                        .withExistingWindowsPlan(plan)
                                        .withExistingResourceGroup(rgName)
                                        .withLocalGitSourceControl()
                                        .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                                        .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                                        .createAsync(),
                                azure.webApps().define(app3Name)
                                        .withExistingWindowsPlan(plan)
                                        .withNewResourceGroup(rgName)
                                        .defineSourceControl()
                                            .withPublicGitRepository(
                                                    "https://github.com/Azure-Samples/app-service-web-dotnet-get-started")
                                            .withBranch("master")
                                            .attach()
                                        .createAsync(),
                                azure.webApps()
                                        .define(app4Name)
                                        .withExistingWindowsPlan(plan)
                                        .withExistingResourceGroup(rgName)
                                        // Uncomment the following lines to turn on 4th scenario
                                        //.defineSourceControl()
                                        //    .withContinuouslyIntegratedGitHubRepository("username", "reponame")
                                        //    .withBranch("master")
                                        //    .withGitHubAccessToken("YOUR GITHUB PERSONAL TOKEN")
                                        //    .attach()
                                        .createAsync());
                    }).flatMap(indexable -> {
                        if (indexable instanceof WebApp) {
                            WebApp app = (WebApp) indexable;
                            System.out.println("Created web app " + app.name());
                            if (!app.name().equals(app2Name)) {
                                return Flux.just(indexable);
                            }
                            // for the second web app Deploy a local Tomcat
                            return app.getPublishingProfileAsync()
                                    .map(profile -> {
                                        System.out.println("Deploying a local Tomcat source to " + app2Name + " through Git...");
                                        Git git = null;
                                        try {
                                            git = Git
                                                    .init()
                                                    .setDirectory(new File(
                                                            ManageWebAppSourceControlAsync.class.getResource(
                                                                        "/azure-samples-appservice-helloworld/")
                                                                    .getPath()))
                                                    .call();
                                            git.add().addFilepattern(".").call();
                                            git.commit().setMessage("Initial commit").call();
                                            PushCommand command = git.push();
                                            command.setRemote(profile.gitUrl());
                                            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(profile.gitUsername(), profile.gitPassword()));
                                            command.setRefSpecs(new RefSpec("master:master"));
                                            command.setForce(true);
                                            command.call();
                                        } catch (GitAPIException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("Deployment to web app " + app2Name + " completed");
                                        return profile;
                                    });
                        }
                        return Flux.just(indexable);
                    });

            Flux.merge(app1Observable, app234Observable).blockLast();

            // warm up
            System.out.println("Warming up " + app1Url + "/helloworld...");
            Utils.curl("http://" + app1Url + "/helloworld/");
            System.out.println("Warming up " + app2Url + "/helloworld...");
            Utils.curl("http://" + app2Url + "/helloworld/");
            System.out.println("Warming up " + app3Url + "...");
            Utils.curl("http://" + app3Url);
            System.out.println("Warming up " + app4Url + "...");
            Utils.curl("http://" + app4Url);
            SdkContext.sleep(5000);
            System.out.println("CURLing " + app1Url + "/helloworld...");
            System.out.println(Utils.curl("http://" + app1Url + "/helloworld/"));
            System.out.println("CURLing " + app2Url + "/helloworld...");
            System.out.println(Utils.curl("http://" + app2Url + "/helloworld/"));
            System.out.println("CURLing " + app3Url + "...");
            System.out.println(Utils.curl("http://" + app3Url));
            System.out.println("CURLing " + app4Url + "...");
            System.out.println(Utils.curl("http://" + app4Url));

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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
