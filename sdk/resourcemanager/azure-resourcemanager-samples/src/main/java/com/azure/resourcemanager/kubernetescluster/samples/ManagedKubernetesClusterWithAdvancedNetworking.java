// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.kubernetescluster.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.network.models.Network;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Azure Container Service (AKS) sample for managing a Kubernetes cluster.
 *   - Create a Virtual Network with two subnets.
 *   - Create a SSH private/public key
 *   - Create an Azure Container Service (AKS) with managed Kubernetes cluster and advanced networking
 *   - Update the number of agent virtual machines in the Kubernetes cluster
 */
public class ManagedKubernetesClusterWithAdvancedNetworking {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param clientId secondary service principal client ID
     * @param secret secondary service principal secret
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId, String secret) throws IOException, JSchException {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgaks", 15);
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnetaks", 20);
        final String aksName = Utils.randomResourceName(azureResourceManager, "akssample", 30);
        final Region region = Region.US_CENTRAL;
        String servicePrincipalClientId = clientId; // replace it with a real service principal client id
        String servicePrincipalSecret = secret; // and the corresponding secret
        final String rootUserName = "aksuser";

        try {

            // ============================================================
            // Create a virtual network with two subnets.
            System.out.println("Create a virtual network with two subnets: subnet1 and subnet2");

            Network virtualNetwork = azureResourceManager.networks().define(vnetName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAddressSpace("192.168.0.0/16")
                .defineSubnet("subnet1")
                    .withAddressPrefix("192.168.1.0/24")
                    .attach()
                .defineSubnet("subnet2")
                    .withAddressPrefix("192.168.2.0/24")
                    .attach()
                .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork);

            //=============================================================
            // If service principal client id and secret are not set via the local variables, attempt to read the service
            //     principal client id and secret from a secondary ".azureauth" file set through an environment variable.
            //
            //     If the environment variable was not set then reuse the main service principal set for running this sample.

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
            System.out.println("SSH private key value: " + sshKeys.getSshPrivateKey());
            System.out.println("SSH public key value: " + sshKeys.getSshPublicKey());


            //=============================================================
            // Create a Kubernetes cluster with one agent pool of size one

            System.out.println("Creating a Kubernetes cluster");

            Date t1 = new Date();

            KubernetesCluster kubernetesCluster = azureResourceManager.kubernetesClusters().define(aksName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withDefaultVersion()
                .withRootUsername(rootUserName)
                .withSshKey(sshKeys.getSshPublicKey())
                .withServicePrincipalClientId(servicePrincipalClientId)
                .withServicePrincipalSecret(servicePrincipalSecret)
                .defineAgentPool("agentpool")
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D1_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withVirtualNetwork(virtualNetwork.id(), "subnet1")
                    .attach()
                .withDnsPrefix("dns-" + aksName)
                .defineNetworkProfile()
                    .withNetworkPlugin(NetworkPlugin.AZURE)
                    .withServiceCidr("10.0.0.0/16")
                    .withDnsServiceIP("10.0.0.10")
                    .withDockerBridgeCidr("172.17.0.1/16")
                    .attach()
                .create();

            Date t2 = new Date();
            System.out.println("Created Azure Container Service (AKS) resource: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + kubernetesCluster.id());
            Utils.print(kubernetesCluster);

            //=============================================================
            // Update a Kubernetes cluster agent pool to two machines

            System.out.println("Updating a Kubernetes cluster agent pool to two virtual machines");

            t1 = new Date();

            kubernetesCluster.update()
                .updateAgentPool("agentpool")
                    .withAgentPoolVirtualMachineCount(2)
                    .parent()
                .apply();

            t2 = new Date();
            System.out.println("Updated Azure Container Service (AKS) resource: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + kubernetesCluster.id());
            Utils.print(kubernetesCluster);

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
