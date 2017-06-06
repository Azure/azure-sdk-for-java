/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.samples;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.DockerUtils;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Azure App Service sample for deploying from an Azure Container Registry.
 *    - Create an Azure Container Registry to be used for holding the Docker images
 *    - If a local Docker engine cannot be found, create a Linux virtual machine that will host a Docker engine
 *        to be used for this sample
 *    - Use Docker Java to create a Docker client that will push/pull an image to/from Azure Container Registry
 *    - Pull a test image from the public Docker repo (tomcat:8-jre8) to be used as a sample for pushing/pulling
 *        to/from an Azure Container Registry
 *    - Deploys to a new web app from the Tomcat image
 */
public class ManageLinuxWebAppWithContainerRegistry {

    private static OkHttpClient httpClient;

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgACR", 15);
        final String acrName = SdkContext.randomResourceName("acrsample", 20);
        final String saName = SdkContext.randomResourceName("sa", 20);
        final String appName = SdkContext.randomResourceName("webapp", 20);
        final String appUrl = appName + ".azurewebsites.net";
        final Region region = Region.US_EAST;
        final String dockerImageName = "tomcat";
        final String dockerImageTag = "8-jre8";
        final String dockerContainerName = "tomcat-private";

        try {
            //=============================================================
            // Create an Azure Container Registry to store and manage private Docker container images

            System.out.println("Creating an Azure Container Registry");

            Date t1 = new Date();

            Registry azureRegistry = azure.containerRegistries().define(acrName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewStorageAccount(saName)
                    .withRegistryNameAsAdminUser()
                    .create();

            Date t2 = new Date();
            System.out.println("Created Azure Container Registry: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureRegistry.id());
            Utils.print(azureRegistry);


            //=============================================================
            // Create a Docker client that will be used to push/pull images to/from the Azure Container Registry

            RegistryListCredentials acrCredentials = azureRegistry.listCredentials();
            DockerClient dockerClient = DockerUtils.createDockerClient(azure, rgName, region,
                    azureRegistry.loginServerUrl(), acrCredentials.username(), acrCredentials.passwords().get(0).value());

            //=============================================================
            // Pull a temp image from public Docker repo and create a temporary container from that image
            // These steps can be replaced and instead build a custom image using a Dockerfile and the app's JAR

            dockerClient.pullImageCmd(dockerImageName)
                    .withTag(dockerImageTag)
                    .exec(new PullImageResultCallback())
                    .awaitSuccess();
            System.out.println("List local Docker images:");
            List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
            for (Image image : images) {
                System.out.format("\tFound Docker image %s (%s)\n", image.getRepoTags()[0], image.getId());
            }

            CreateContainerResponse dockerContainerInstance = dockerClient.createContainerCmd(dockerImageName + ":" + dockerImageTag)
                    .withName(dockerContainerName)
                    .exec();

            //=============================================================
            // Commit the new container

            String privateRepoUrl = azureRegistry.loginServerUrl() + "/samples/" + dockerContainerName;
            String dockerImageId = dockerClient.commitCmd(dockerContainerInstance.getId())
                    .withRepository(privateRepoUrl)
                    .withTag("latest").exec();

            // We can now remove the temporary container instance
            dockerClient.removeContainerCmd(dockerContainerInstance.getId())
                    .withForce(true)
                    .exec();

            //=============================================================
            // Push the new Docker image to the Azure Container Registry

            dockerClient.pushImageCmd(privateRepoUrl)
                    .withAuthConfig(dockerClient.authConfig())
                    .exec(new PushImageResultCallback()).awaitSuccess();

            // Remove the temp image from the local Docker host
            try {
                dockerClient.removeImageCmd(dockerImageName + ":" + dockerImageTag).withForce(true).exec();
            } catch (NotFoundException e) {
                // just ignore if not exist
            }

            //============================================================
            // Create a web app with a new app service plan

            System.out.println("Creating web app " + appName + " in resource group " + rgName + "...");

            WebApp app = azure.webApps().define(appName)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .withNewLinuxPlan(PricingTier.STANDARD_S1)
                    .withPrivateRegistryImage(privateRepoUrl + ":latest", "http://" + azureRegistry.loginServerUrl())
                    .withCredentials(acrCredentials.username(), acrCredentials.passwords().get(0).value())
                    .withAppSetting("PORT", "8080")
                    .create();

            System.out.println("Created web app " + app.name());
            Utils.print(app);

            // warm up
            System.out.println("Warming up " + appUrl + "...");
            curl("http://" + appUrl);
            Thread.sleep(5000);
            System.out.println("CURLing " + appUrl + "...");
            System.out.println(curl("http://" + appUrl));

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
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
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
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

    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }
}