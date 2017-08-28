/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.LogLevel;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.RegistryAuth;

import java.io.File;
import java.nio.file.Paths;

/**
 * Azure Container Instance sample for managing container instances.
 *    - Create an Azure container instance
 *    - Create an Azure Container Registry to be used for holding the Docker images
 *    - Create an Azure Container Service with Docker Swarm orchestration
 *    - Use Spotify Docker Client to create a Docker client that will push/pull an image to/from Azure Container Registry
 *    - Pull a test image from the public Docker repo (hello-world:latest) to be used as a sample for pushing/pulling
 *        to/from an Azure Container Registry
 *    - Create a container instance from an image that was pulled from Azure Container Registry
 */
public class ManageContainerInstance {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgACI", 15);
        final String aciName = SdkContext.randomResourceName("acisample", 20);

        try {
            final String envDockerHost = System.getenv("DOCKER_HOST");
            final String envDockerCertPath = System.getenv("DOCKER_CERT_PATH");
            DockerClient dockerClient;

            if (envDockerHost != null && !envDockerHost.isEmpty()) {
                dockerClient = DefaultDockerClient.fromEnv().build();
            } else {
                boolean usePrivateRegistry = true;
                boolean dockerHostTlsEnabled = true;
                String dockerHostUrl = dockerHostTlsEnabled ? "https://localhost:2376" : "http://localhost:2375";
                String tempCertPath = "";

                DefaultDockerClient.Builder builder = new DefaultDockerClient.Builder();
                builder.uri(dockerHostUrl);

                if (dockerHostTlsEnabled) {
                    builder.dockerCertificates(new DockerCertificates(Paths.get(tempCertPath)));
                }
                if (usePrivateRegistry) {
                    builder.registryAuth(RegistryAuth.create(
                        "username", "password", "test@email.com", "server-address", "", null
                        ));
                }
                dockerClient = builder.build();
                dockerClient.pull("hello-world");
                dockerClient.listImages(DockerClient.ListImagesParam.allImages());

                dockerClient.close();

            }

            //=============================================================

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
}
