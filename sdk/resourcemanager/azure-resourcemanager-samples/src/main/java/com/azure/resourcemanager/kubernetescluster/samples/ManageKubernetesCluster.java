// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.kubernetescluster.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.Date;

/**
 * Azure Container Service (AKS) sample for managing a Kubernetes cluster.
 *   - Create an Azure Container Service (AKS) with managed Kubernetes cluster
 *   - Create a SSH private/public key
 *   - Update the number of agent virtual machines in the Kubernetes cluster
 */
public class ManageKubernetesCluster {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgaks", 15);
        final String aksName = Utils.randomResourceName(azureResourceManager, "akssample", 30);
        final Region region = Region.US_EAST;
        final String rootUserName = "aksuser";

        try {
            //=============================================================
            // Create a Kubernetes cluster with one agent pool of size one

            System.out.println("Creating a Kubernetes cluster");

            Date t1 = new Date();

            KubernetesCluster kubernetesCluster = azureResourceManager.kubernetesClusters().define(aksName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool("agentpool")
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .attach()
                .withDnsPrefix("dns-" + aksName)
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

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
