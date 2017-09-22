/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
//import com.spotify.docker.client.DefaultDockerClient;
//import com.spotify.docker.client.DockerCertificates;
//import com.spotify.docker.client.DockerClient;
//import com.spotify.docker.client.messages.RegistryAuth;

import java.io.File;
//import java.nio.file.Paths;
//import java.util.List;

/**
 * Azure Container Instance sample for managing container instances.
 *    - Create an Azure container instance using Docker image "microsoft/aci-helloworld" with a mount to the file share from above
 *    - Test that the container app can be reached via "curl" like HTTP GET call
 *    - Retrieve container log content
 */
public class ManageContainerInstanceZeroToOneAndOneToManyUsingDockerSwarmOrchestration {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgACI", 15);
        final String aciName = SdkContext.randomResourceName("acisample", 20);
        final String saName = SdkContext.randomResourceName("sa", 20);
        final String containerImageName = "microsoft/aci-helloworld";

        try {
//            final String envDockerHost = System.getenv("DOCKER_HOST");
//            final String envDockerCertPath = System.getenv("DOCKER_CERT_PATH");
//            DockerClient dockerClient;
//
//            if (envDockerHost != null && !envDockerHost.isEmpty()) {
//                dockerClient = DefaultDockerClient.fromEnv().build();
//            } else {
//                boolean usePrivateRegistry = true;
//                boolean dockerHostTlsEnabled = true;
//                String dockerHostUrl = dockerHostTlsEnabled ? "https://localhost:2376" : "http://localhost:2375";
//                String tempCertPath = "";
//
//                DefaultDockerClient.Builder builder = new DefaultDockerClient.Builder();
//                builder.uri(dockerHostUrl);
//
//                if (dockerHostTlsEnabled) {
//                    builder.dockerCertificates(new DockerCertificates(Paths.get(tempCertPath)));
//                }
//                if (usePrivateRegistry) {
//                    builder.registryAuth(RegistryAuth.create(
//                        "username", "password", "test@email.com", "server-address", "", null
//                        ));
//                }
//                dockerClient = builder.build();
//                dockerClient.pull("hello-world");
//                dockerClient.listImages(DockerClient.ListImagesParam.allImages());
//
//                dockerClient.close();
//
//            }

            //=============================================================
            //=============================================================
            // Create a container group with one container instance of default CPU core count and memory size
            //   using public Docker image "microsoft/aci-helloworld" which mounts the file share created previously
            //   as read/write shared container volume.

            ContainerGroup containerGroup = azure.containerGroups().define(aciName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .defineContainerInstance(aciName)
                .withImage(containerImageName)
                .withExternalTcpPort(80)
                .attach()
                .create();

            Utils.print(containerGroup);

            //=============================================================
            // Check that the container instance is up and running

            // warm up
            System.out.println("Warming up " + containerGroup.ipAddress());
            Utils.curl("http://" + containerGroup.ipAddress());
            Thread.sleep(15000);
            System.out.println("CURLing " + containerGroup.ipAddress());
            System.out.println(Utils.curl("http://" + containerGroup.ipAddress()));

            //=============================================================
            // Check the container instance logs

            String logContent = containerGroup.getLogContent(aciName);
            System.out.format("Logs for container instance: %s\n%s", aciName, logContent);

            //=============================================================
            // Remove the container group

            azure.containerGroups().deleteById(containerGroup.id());

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
