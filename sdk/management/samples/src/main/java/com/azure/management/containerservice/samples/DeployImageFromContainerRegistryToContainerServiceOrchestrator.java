/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerservice.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.management.Azure;
import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServiceMasterProfileCount;
import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.SSHShell;
import com.azure.management.samples.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Azure Container Registry sample for deploying a container image to Azure Container Service with Kubernetes orchestration.
 *  - Create an Azure Container Registry to be used for holding the Docker images
 *  - If a local Docker engine cannot be found, create a Linux virtual machine that will host a Docker engine to be used for this sample
 *  - Use Docker Java to create a Docker client that will push/pull an image to/from Azure Container Registry
 *  - Pull a test image from the public Docker repo (tomcat:8) to be used as a sample for pushing/pulling to/from an Azure Container Registry
 *  - Create a new Docker container from an image that was pulled from Azure Container Registry
 *  - Create a SSH private/public key to be used when creating a container service
 *  - Create an Azure Container Service with Kubernetes orchestration
 *  - Log in via the SSH client and download the Kubernetes config
 *  - Create a Kubernetes client using the Kubernetes config file downloaded from one of the virtual machine managers
 *  - Create a Kubernetes namespace
 *  - Create a Kubernetes secret of type "docker-registry" using the Azure Container Registry credentials from above
 *  - Create a Kubernetes replication controller using a container image from the Azure private registry from above and a load balancer service that will expose the app to the world
 */
public class DeployImageFromContainerRegistryToContainerServiceOrchestrator {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @param clientId secondary service principal client ID
     * @param secret secondary service principal secret
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String clientId, String secret) {
        final String rgName = azure.sdkContext().randomResourceName("rgACR", 15);
        final String acrName = azure.sdkContext().randomResourceName("acrsample", 20);
        final String acsName = azure.sdkContext().randomResourceName("acssample", 30);
        final String rootUserName = "acsuser";
        final Region region = Region.US_EAST;
        final String dockerImageName = "nginx";
        final String dockerImageTag = "latest";
        final String dockerContainerName = "acrsample-nginx";
        String acsSecretName = "mysecret112233";
        String acsNamespace = "acrsample";
        String acsLbIngressName = "lb-acrsample";
        String servicePrincipalClientId = clientId; // replace with a real service principal client id
        String servicePrincipalSecret = secret; // and corresponding secret
        SSHShell shell = null;

        try {

            //=============================================================
            // If service principal client id and secret are not set via the local variables, attempt to read the service
            //   principal client id and secret from a secondary ".azureauth" file set through an environment variable.
            //
            //   If the environment variable was not set then reuse the main service principal set for running this sample.

            if (servicePrincipalClientId.isEmpty() || servicePrincipalSecret.isEmpty()) {
                servicePrincipalClientId = System.getenv("AZURE_CLIENT_ID");
                servicePrincipalSecret = System.getenv("AZURE_CLIENT_SECRET");
                if (servicePrincipalClientId.isEmpty() || servicePrincipalSecret.isEmpty()) {
                    String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");

                    if (envSecondaryServicePrincipal == null || !envSecondaryServicePrincipal.isEmpty() || !Files.exists(Paths.get(envSecondaryServicePrincipal))) {
                        envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
                    }

                    servicePrincipalClientId = Utils.getSecondaryServicePrincipalClientID(envSecondaryServicePrincipal);
                    servicePrincipalSecret = Utils.getSecondaryServicePrincipalSecret(envSecondaryServicePrincipal);
                }
            }


            //=============================================================
            // Create an SSH private/public key pair to be used when creating the container service

            System.out.println("Creating an SSH private and public key pair");

            SSHShell.SshPublicPrivateKey sshKeys = SSHShell.generateSSHKeys("", "ACS");
            System.out.println("SSH private key value: \n" + sshKeys.getSshPrivateKey());
            System.out.println("SSH public key value: \n" + sshKeys.getSshPublicKey());


            //=============================================================
            // Create an Azure Container Service with Kubernetes orchestration

            System.out.println("Creating an Azure Container Service with Kubernetes ochestration and one agent (virtual machine)");

            Date t1 = new Date();

            ContainerService azureContainerService = azure.containerServices().define(acsName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withKubernetesOrchestration()
                    .withServicePrincipal(servicePrincipalClientId, servicePrincipalSecret)
                    .withLinux()
                    .withRootUsername(rootUserName)
                    .withSshKey(sshKeys.getSshPublicKey())
                    .withMasterNodeCount(ContainerServiceMasterProfileCount.MIN)
                    .defineAgentPool("agentpool")
                        .withVirtualMachineCount(1)
                        .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D1_V2)
                        .withDnsPrefix("dns-ap-" + acsName)
                        .attach()
                    .withMasterDnsPrefix("dns-" + acsName)
                .create();

            Date t2 = new Date();
            System.out.println("Created Azure Container Service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureContainerService.id());
            Utils.print(azureContainerService);


            //=============================================================
            // Create an Azure Container Registry to store and manage private Docker container images

            // TODO: add registry
//            System.out.println("Creating an Azure Container Registry");
//
//            t1 = new Date();
//
//            Registry azureRegistry = azure.containerRegistries().define(acrName)
//                    .withRegion(region)
//                    .withNewResourceGroup(rgName)
//                    .withBasicSku()
//                    .withRegistryNameAsAdminUser()
//                    .create();
//
//            t2 = new Date();
//            System.out.println("Created Azure Container Registry: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureRegistry.id());
//            Utils.print(azureRegistry);
//
//
//            //=============================================================
//            // Create a Docker client that will be used to push/pull images to/from the Azure Container Registry
//
//            RegistryCredentials acrCredentials = azureRegistry.getCredentials();
//            DockerClient dockerClient = DockerUtils.createDockerClient(azure, rgName, region,
//                    azureRegistry.loginServerUrl(), acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY));
//
//            //=============================================================
//            // Pull a temp image from public Docker repo and create a temporary container from that image
//            // These steps can be replaced and instead build a custom image using a Dockerfile and the app's JAR
//
//            dockerClient.pullImageCmd(dockerImageName)
//                    .withTag(dockerImageTag)
//                    .exec(new PullImageResultCallback())
//                    .awaitSuccess();
//            System.out.println("List local Docker images:");
//            List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
//            for (Image image : images) {
//                System.out.format("\tFound Docker image %s (%s)\n", image.getRepoTags()[0], image.getId());
//            }
//
//            CreateContainerResponse dockerContainerInstance = dockerClient.createContainerCmd(dockerImageName + ":" + dockerImageTag)
//                    .withName(dockerContainerName)
//                    .withCmd("/hello")
//                    .exec();
//            System.out.println("List Docker containers:");
//            List<Container> dockerContainers = dockerClient.listContainersCmd()
//                    .withShowAll(true)
//                    .exec();
//            for (Container container : dockerContainers) {
//                System.out.format("\tFound Docker container %s (%s)\n", container.getImage(), container.getId());
//            }
//
//            //=============================================================
//            // Commit the new container
//
//            String privateRepoUrl = azureRegistry.loginServerUrl() + "/samples/" + dockerContainerName;
//            String dockerImageId = dockerClient.commitCmd(dockerContainerInstance.getId())
//                    .withRepository(privateRepoUrl)
//                    .withTag("latest").exec();
//
//            // We can now remove the temporary container instance
//            dockerClient.removeContainerCmd(dockerContainerInstance.getId())
//                    .withForce(true)
//                    .exec();
//
//            //=============================================================
//            // Push the new Docker image to the Azure Container Registry
//
//            dockerClient.pushImageCmd(privateRepoUrl)
//                    .withAuthConfig(dockerClient.authConfig())
//                    .exec(new PushImageResultCallback()).awaitSuccess();
//
//            // Remove the temp image from the local Docker host
//            try {
//                dockerClient.removeImageCmd(dockerImageName + ":" + dockerImageTag).withForce(true).exec();
//            } catch (NotFoundException e) {
//                // just ignore if not exist
//            }
//
//            //=============================================================
//            // Verify that the image we saved in the Azure Container registry can be pulled and instantiated locally
//
//            dockerClient.pullImageCmd(privateRepoUrl)
//                    .withAuthConfig(dockerClient.authConfig())
//                    .exec(new PullImageResultCallback()).awaitSuccess();
//            System.out.println("List local Docker images after pulling sample image from the Azure Container Registry:");
//            images = dockerClient.listImagesCmd()
//                    .withShowAll(true)
//                    .exec();
//            for (Image image : images) {
//                System.out.format("\tFound Docker image %s (%s)\n", image.getRepoTags()[0], image.getId());
//            }
//            dockerContainerInstance = dockerClient.createContainerCmd(privateRepoUrl)
//                    .withName(dockerContainerName + "-private")
//                    .withCmd("/hello").exec();
//            System.out.println("List Docker containers after instantiating container from the Azure Container Registry sample image:");
//            dockerContainers = dockerClient.listContainersCmd()
//                    .withShowAll(true)
//                    .exec();
//            for (Container container : dockerContainers) {
//                System.out.format("\tFound Docker container %s (%s)\n", container.getImage(), container.getId());
//            }
//
//
//            //=============================================================
//            // Download the Kubernetes config file from one of the master virtual machines
//
//            azureContainerService = azure.containerServices().getByResourceGroup(rgName, acsName);
//            System.out.println("Found Kubernetes master at: " + azureContainerService.masterFqdn());
//
//            shell = SSHShell.open(azureContainerService.masterFqdn(), 22, rootUserName, sshKeys.getSshPrivateKey().getBytes());
//
//            String kubeConfigContent = shell.download("config", ".kube", true);
//            System.out.println("Found Kubernetes config:\n" + kubeConfigContent);
//
//
//            //=============================================================
//            // Instantiate the Kubernetes client using the downloaded ".kube/config" file content
//            //     The Kubernetes client API requires setting an environment variable pointing at a real file;
//            //        we will create a temporary file that will be deleted automatically when the sample exits
//
//            File tempKubeConfigFile = File.createTempFile("kube", ".config", new File(System.getProperty("java.io.tmpdir")));
//            tempKubeConfigFile.deleteOnExit();
//            BufferedWriter buffOut = new BufferedWriter(new FileWriter(tempKubeConfigFile));
//            buffOut.write(kubeConfigContent);
//            buffOut.close();
//
//            System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, tempKubeConfigFile.getPath());
//            Config config = new Config();
//            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
//
//
//            //=============================================================
//            // List all the nodes available in the Kubernetes cluster
//
//            System.out.println(kubernetesClient.nodes().list());
//
//
//            //=============================================================
//            // Create a namespace where all the sample Kubernetes resources will be created
//
//            Namespace ns = new NamespaceBuilder()
//                    .withNewMetadata()
//                        .withName(acsNamespace)
//                        .addToLabels("acr", "sample")
//                    .endMetadata()
//                    .build();
//            try {
//                System.out.println("Created namespace" + kubernetesClient.namespaces().create(ns));
//            } catch (Exception ignored) {
//            }
//
//            SdkContext.sleep(5000);
//            for (Namespace namespace : kubernetesClient.namespaces().list().getItems()) {
//                System.out.println("\tFound Kubernetes namespace: " + namespace.toString());
//            }
//
//
//            //=============================================================
//            // Create a secret of type "docker-repository" that will be used for downloading the container image from
//            //     our Azure private container repo
//
//            String basicAuth = new String(Base64.encodeBase64((acrCredentials.username() + ":" + acrCredentials.accessKeys().get(AccessKeyType.PRIMARY)).getBytes()));
//            HashMap<String, String> secretData = new HashMap<>(1);
//            String dockerCfg = String.format("{ \"%s\": { \"auth\": \"%s\", \"email\": \"%s\" } }",
//                    azureRegistry.loginServerUrl(),
//                    basicAuth,
//                    "acrsample@azure.com");
//
//            dockerCfg = new String(Base64.encodeBase64(dockerCfg.getBytes("UTF-8")), "UTF-8");
//            secretData.put(".dockercfg", dockerCfg);
//            SecretBuilder secretBuilder = new SecretBuilder()
//                    .withNewMetadata()
//                        .withName(acsSecretName)
//                        .withNamespace(acsNamespace)
//                    .endMetadata()
//                    .withData(secretData)
//                    .withType("kubernetes.io/dockercfg");
//
//            System.out.println("Creating new secret: " + kubernetesClient.secrets().inNamespace(acsNamespace).create(secretBuilder.build()));
//
//            SdkContext.sleep(5000);
//
//            for (Secret kubeS : kubernetesClient.secrets().inNamespace(acsNamespace).list().getItems()) {
//                System.out.println("\tFound secret: " + kubeS);
//            }
//
//
//            //=============================================================
//            // Create a replication controller for our image stored in the Azure Container Registry
//
//            ReplicationController rc = new ReplicationControllerBuilder()
//                    .withNewMetadata()
//                        .withName("acrsample-rc")
//                        .withNamespace(acsNamespace)
//                        .addToLabels("acrsample-nginx", "nginx")
//                    .endMetadata()
//                    .withNewSpec()
//                        .withReplicas(2)
//                        .withNewTemplate()
//                            .withNewMetadata()
//                                .addToLabels("acrsample-nginx", "nginx")
//                            .endMetadata()
//                            .withNewSpec()
//                                .addNewImagePullSecret(acsSecretName)
//                                .addNewContainer()
//                                    .withName("acrsample-pod-nginx")
//                                    .withImage(privateRepoUrl)
//                                    .addNewPort()
//                                        .withContainerPort(80)
//                                    .endPort()
//                                .endContainer()
//                            .endSpec()
//                        .endTemplate()
//                    .endSpec()
//                    .build();
//
//            System.out.println("Creating a replication controller: " + kubernetesClient.replicationControllers().inNamespace(acsNamespace).create(rc));
//            SdkContext.sleep(5000);
//
//            rc = kubernetesClient.replicationControllers().inNamespace(acsNamespace).withName("acrsample-rc").get();
//            System.out.println("Found replication controller: " + rc.toString());
//
//            for (Pod pod : kubernetesClient.pods().inNamespace(acsNamespace).list().getItems()) {
//                System.out.println("\tFound Kubernetes pods: " + pod.toString());
//            }
//
//
//            //=============================================================
//            // Create a Load Balancer service that will expose the service to the world
//
//            Service lbService = new ServiceBuilder()
//                    .withNewMetadata()
//                        .withName(acsLbIngressName)
//                        .withNamespace(acsNamespace)
//                    .endMetadata()
//                    .withNewSpec()
//                        .withType("LoadBalancer")
//                        .addNewPort()
//                            .withPort(80)
//                            .withProtocol("TCP")
//                        .endPort()
//                        .addToSelector("acrsample-nginx", "nginx")
//                    .endSpec()
//                    .build();
//
//            System.out.println("Creating a service: " + kubernetesClient.services().inNamespace(acsNamespace).create(lbService));
//
//            SdkContext.sleep(5000);
//
//            System.out.println("\tFound service: " + kubernetesClient.services().inNamespace(acsNamespace).withName(acsLbIngressName).get());
//
//
//            //=============================================================
//            // Wait until the external IP becomes available
//
//            int timeout = 30 * 60 * 1000; // 30 minutes
//            String matchIPV4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
//
//            while (timeout > 0) {
//                try {
//                    List<LoadBalancerIngress> lbIngressList = kubernetesClient.services().inNamespace(acsNamespace).withName(acsLbIngressName).get().getStatus().getLoadBalancer().getIngress();
//                    if (lbIngressList != null && !lbIngressList.isEmpty() && lbIngressList.get(0) != null && lbIngressList.get(0).getIp().matches(matchIPV4)) {
//                        System.out.println("\tFound ingress IP: " + lbIngressList.get(0).getIp());
//                        timeout = 0;
//                    }
//                } catch (Exception ignored) {
//                }
//
//                if (timeout > 0) {
//                    timeout -= 30000; // 30 seconds
//                    SdkContext.sleep(30000);
//                }
//            }
//
//            // Clean-up
//            kubernetesClient.namespaces().delete(ns);
//
//            shell.close();
//            shell = null;

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
                if (shell != null) {
                    shell.close();
                }
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
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, "", "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
