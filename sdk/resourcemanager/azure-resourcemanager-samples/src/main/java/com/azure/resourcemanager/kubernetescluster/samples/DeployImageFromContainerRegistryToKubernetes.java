// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.kubernetescluster.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.DockerUtils;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.jcraft.jsch.JSchException;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Azure Container Registry sample for deploying a container image to Azure Container Service with Kubernetes orchestration.
 *  - Create an Azure Container Registry to be used for holding the Docker images
 *  - If a local Docker engine cannot be found, create a Linux virtual machine that will host a Docker engine to be used for this sample
 *  - Use Docker Java to create a Docker client that will push/pull an image to/from Azure Container Registry
 *  - Pull a test image from the public Docker repo (tomcat:8) to be used as a sample for pushing/pulling to/from an Azure Container Registry
 *  - Create a new Docker container from an image that was pulled from Azure Container Registry
 *  - Create a SSH private/public key to be used when creating a container service
 *  - Create an Azure Container Service (AKS) resource
 *  - Log in via the SSH client and download the Kubernetes config
 *  - Create a Kubernetes client using the Kubernetes config file downloaded from one of the virtual machine managers
 *  - Create a Kubernetes namespace
 *  - Create a Kubernetes secret of type "docker-registry" using the Azure Container Registry credentials from above
 *  - Create a Kubernetes replication controller using a container image from the Azure private registry from above and a load balancer service that will expose the app to the world
 */
public class DeployImageFromContainerRegistryToKubernetes {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param clientId secondary service principal client ID
     * @param secret secondary service principal secret
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId, String secret) throws IOException, JSchException, InterruptedException {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgaks", 15);
        final String acrName = Utils.randomResourceName(azureResourceManager, "acrsample", 20);
        final String aksName = Utils.randomResourceName(azureResourceManager, "akssample", 30);
        final String rootUserName = "aksuser";
        final Region region = Region.US_EAST;
        final String dockerImageName = "nginx";
        final String dockerImageTag = "latest";
        final String dockerContainerName = "acrsample-nginx";
        String aksSecretName = "mysecret112233";
        String aksNamespace = "acrsample";
        String aksLbIngressName = "lb-acrsample";
        String servicePrincipalClientId = clientId; // replace with a real service principal client id
        String servicePrincipalSecret = secret; // and corresponding secret

        try {

            //=============================================================
            // If service principal client id and secret are not set via the local variables, attempt to read the service
            //   principal client id and secret from a secondary ".azureauth" file set through an environment variable.
            //
            //   If the environment variable was not set then reuse the main service principal set for running this sample.

            if (servicePrincipalClientId == null || servicePrincipalClientId.isEmpty() || servicePrincipalSecret == null || servicePrincipalSecret.isEmpty()) {
                servicePrincipalClientId = System.getenv("AZURE_CLIENT_ID");
                servicePrincipalSecret = System.getenv("AZURE_CLIENT_SECRET");
                if (servicePrincipalClientId == null || servicePrincipalClientId.isEmpty() || servicePrincipalSecret == null || servicePrincipalSecret.isEmpty()) {
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
            System.out.println("SSH private key value: %n" + sshKeys.getSshPrivateKey());
            System.out.println("SSH public key value: %n" + sshKeys.getSshPublicKey());


            //=============================================================
            // Create an Azure Container Service (AKS) with managed Kubernetes clusters

            System.out.println("Creating an Azure Container Service with managed Kubernetes cluster and one agent pool with one virtual machine");

            Date t1 = new Date();

            KubernetesCluster kubernetesCluster = azureResourceManager.kubernetesClusters().define(aksName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername(rootUserName)
                .withSshKey(sshKeys.getSshPublicKey())
                .withServicePrincipalClientId(servicePrincipalClientId)
                .withServicePrincipalSecret(servicePrincipalSecret)
                .defineAgentPool("agentpool")
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .attach()
                .withDnsPrefix("dns-" + aksName)
                .create();

            Date t2 = new Date();
            System.out.println("Created Azure Container Service (AKS) resource: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + kubernetesCluster.id());
            Utils.print(kubernetesCluster);


            //=============================================================
            // Create an Azure Container Registry to store and manage private Docker container images

            System.out.println("Creating an Azure Container Registry");

            t1 = new Date();

            Registry azureRegistry = azureResourceManager.containerRegistries().define(acrName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withBasicSku()
                .withRegistryNameAsAdminUser()
                .create();

            t2 = new Date();
            System.out.println("Created Azure Container Registry: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + azureRegistry.id());
            Utils.print(azureRegistry);


            //=============================================================
            // Create a Docker client that will be used to push/pull images to/from the Azure Container Registry

            RegistryCredentials acrCredentials = azureRegistry.getCredentials();
            DockerClient dockerClient = DockerUtils.createDockerClient(azureResourceManager, rgName, region,
                azureRegistry.loginServerUrl(), acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY));


            //=============================================================
            // Pull a temp image from public Docker repo and create a temporary container from that image
            // These steps can be replaced and instead build a custom image using a Dockerfile and the app's JAR

            dockerClient.pullImageCmd(dockerImageName)
                .withTag(dockerImageTag)
                .withAuthConfig(new AuthConfig())
                .exec(new PullImageResultCallback())
                .awaitCompletion();
            System.out.println("List local Docker images:");
            List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
            for (Image image : images) {
                System.out.format("\tFound Docker image %s (%s)%n", image.getRepoTags()[0], image.getId());
            }

            CreateContainerResponse dockerContainerInstance = dockerClient.createContainerCmd(dockerImageName + ":" + dockerImageTag)
                .withName(dockerContainerName)
                .withCmd("/hello")
                .exec();
            System.out.println("List Docker containers:");
            List<Container> dockerContainers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();
            for (Container container : dockerContainers) {
                System.out.format("\tFound Docker container %s (%s)%n", container.getImage(), container.getId());
            }


            //=============================================================
            // Commit the new container

            String privateRepoUrl = azureRegistry.loginServerUrl() + "/samples/" + dockerContainerName;
            dockerClient.commitCmd(dockerContainerInstance.getId())
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


            //=============================================================
            // Verify that the image we saved in the Azure Container registry can be pulled and instantiated locally

            dockerClient.pullImageCmd(privateRepoUrl)
                .withAuthConfig(dockerClient.authConfig())
                .exec(new PullImageResultCallback()).awaitCompletion();
            System.out.println("List local Docker images after pulling sample image from the Azure Container Registry:");
            images = dockerClient.listImagesCmd()
                .withShowAll(true)
                .exec();
            for (Image image : images) {
                System.out.format("\tFound Docker image %s (%s)%n", image.getRepoTags()[0], image.getId());
            }
            dockerClient.createContainerCmd(privateRepoUrl)
                .withName(dockerContainerName + "-private")
                .withCmd("/hello").exec();
            System.out.println("List Docker containers after instantiating container from the Azure Container Registry sample image:");
            dockerContainers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();
            for (Container container : dockerContainers) {
                System.out.format("\tFound Docker container %s (%s)%n", container.getImage(), container.getId());
            }


            //=============================================================
            // Instantiate the Kubernetes client using the ".kube/config" file content from the Kubernetes cluster
            //     The Kubernetes client API requires setting an environment variable pointing at a real file;
            //        we will create a temporary file that will be deleted automatically when the sample exits

            kubernetesCluster = azureResourceManager.kubernetesClusters().getByResourceGroup(rgName, aksName);
            System.out.println("Found Kubernetes master at: " + kubernetesCluster.fqdn());

            byte[] kubeConfigContent = kubernetesCluster.adminKubeConfigContent();
            File tempKubeConfigFile = File.createTempFile("kube", ".config", new File(System.getProperty("java.io.tmpdir")));
            tempKubeConfigFile.deleteOnExit();
            try (BufferedWriter buffOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempKubeConfigFile), StandardCharsets.UTF_8))) {
                buffOut.write(new String(kubeConfigContent, StandardCharsets.UTF_8));
            }

            System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, tempKubeConfigFile.getPath());
            Config config = new Config();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);


            //=============================================================
            // List all the nodes available in the Kubernetes cluster

            System.out.println(kubernetesClient.nodes().list());


            //=============================================================
            // Create a namespace where all the sample Kubernetes resources will be created

            Namespace ns = new NamespaceBuilder()
                .withNewMetadata()
                    .withName(aksNamespace)
                    .addToLabels("acr", "sample")
                    .endMetadata()
                .build();
            try {
                System.out.println("Created namespace" + kubernetesClient.namespaces().create(ns));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            for (Namespace namespace : kubernetesClient.namespaces().list().getItems()) {
                System.out.println("\tFound Kubernetes namespace: " + namespace.toString());
            }


            //=============================================================
            // Create a secret of type "docker-repository" that will be used for downloading the container image from
            //     our Azure private container repo

            String basicAuth = new String(Base64.encodeBase64((acrCredentials.username() + ":" + acrCredentials.accessKeys().get(AccessKeyType.PRIMARY)).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            HashMap<String, String> secretData = new HashMap<>(1);
            String dockerCfg = String.format("{ \"%s\": { \"auth\": \"%s\", \"email\": \"%s\" } }",
                azureRegistry.loginServerUrl(),
                basicAuth,
                "acrsample@azure.com");

            dockerCfg = new String(Base64.encodeBase64(dockerCfg.getBytes("UTF-8")), "UTF-8");
            secretData.put(".dockercfg", dockerCfg);
            SecretBuilder secretBuilder = new SecretBuilder()
                .withNewMetadata()
                    .withName(aksSecretName)
                    .withNamespace(aksNamespace)
                    .endMetadata()
                .withData(secretData)
                .withType("kubernetes.io/dockercfg");

            System.out.println("Creating new secret: " + kubernetesClient.secrets().inNamespace(aksNamespace).create(secretBuilder.build()));

            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            for (Secret kubeS : kubernetesClient.secrets().inNamespace(aksNamespace).list().getItems()) {
                System.out.println("\tFound secret: " + kubeS);
            }


            //=============================================================
            // Create a replication controller for our image stored in the Azure Container Registry

            ReplicationController rc = new ReplicationControllerBuilder()
                .withNewMetadata()
                    .withName("acrsample-rc")
                    .withNamespace(aksNamespace)
                    .addToLabels("acrsample-nginx", "nginx")
                    .endMetadata()
                .withNewSpec()
                    .withReplicas(2)
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("acrsample-nginx", "nginx")
                            .endMetadata()
                        .withNewSpec()
                            .addNewImagePullSecret(aksSecretName)
                            .addNewContainer()
                                .withName("acrsample-pod-nginx")
                                .withImage(privateRepoUrl)
                                .addNewPort()
                                    .withContainerPort(80)
                                    .endPort()
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                .build();

            System.out.println("Creating a replication controller: " + kubernetesClient.replicationControllers().inNamespace(aksNamespace).create(rc));
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            rc = kubernetesClient.replicationControllers().inNamespace(aksNamespace).withName("acrsample-rc").get();
            System.out.println("Found replication controller: " + rc.toString());

            for (Pod pod : kubernetesClient.pods().inNamespace(aksNamespace).list().getItems()) {
                System.out.println("\tFound Kubernetes pods: " + pod.toString());
            }


            //=============================================================
            // Create a Load Balancer service that will expose the service to the world

            Service lbService = new ServiceBuilder()
                .withNewMetadata()
                    .withName(aksLbIngressName)
                    .withNamespace(aksNamespace)
                    .endMetadata()
                .withNewSpec()
                    .withType("LoadBalancer")
                    .addNewPort()
                        .withPort(80)
                        .withProtocol("TCP")
                        .endPort()
                    .addToSelector("acrsample-nginx", "nginx")
                    .endSpec()
                .build();

            System.out.println("Creating a service: " + kubernetesClient.services().inNamespace(aksNamespace).create(lbService));

            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            System.out.println("\tFound service: " + kubernetesClient.services().inNamespace(aksNamespace).withName(aksLbIngressName).get());


            //=============================================================
            // Wait until the external IP becomes available

            int timeout = 30 * 60 * 1000; // 30 minutes
            String matchIPV4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";

            while (timeout > 0) {
                try {
                    List<LoadBalancerIngress> lbIngressList = kubernetesClient.services().inNamespace(aksNamespace).withName(aksLbIngressName).get().getStatus().getLoadBalancer().getIngress();
                    if (lbIngressList != null && !lbIngressList.isEmpty() && lbIngressList.get(0) != null && lbIngressList.get(0).getIp().matches(matchIPV4)) {
                        System.out.println("\tFound ingress IP: " + lbIngressList.get(0).getIp());
                        timeout = 0;
                    }
                } catch (Exception e) {
                }

                if (timeout > 0) {
                    timeout -= 30000; // 30 seconds
                    ResourceManagerUtils.sleep(Duration.ofSeconds(30));
                }
            }

            // Clean-up
            kubernetesClient.namespaces().delete(ns);

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
     *
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

            runSample(azureResourceManager, "", "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
