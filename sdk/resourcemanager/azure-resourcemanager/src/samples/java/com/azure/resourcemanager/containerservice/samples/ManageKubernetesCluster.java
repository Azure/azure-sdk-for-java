// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure Kubernetes Service (AKS) sample for managing a Kubernetes cluster.
 *  - Create a managed Kubernetes cluster (AKS) with a system-assigned managed identity
 *  - Scale the agent pool
 * <p>
 * This sample replaces the retired Azure Container Service (ACS) sample. ACS was deprecated in favor of AKS, which
 * uses a system-assigned managed identity instead of a service principal.
 */
public final class ManageKubernetesCluster {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgaks", 15);
        final String aksName = SampleUtils.randomResourceName(azureResourceManager, "akssample", 30);
        final Region region = Region.US_WEST3;

        try {
            // Create a Kubernetes cluster with a single-node system agent pool.
            KubernetesCluster kubernetesCluster = azureResourceManager.kubernetesClusters()
                .define(aksName)
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

            System.out.println("Created Kubernetes cluster: " + kubernetesCluster.id());

            // Scale the agent pool to two nodes.
            kubernetesCluster.update()
                .updateAgentPool("agentpool")
                .withAgentPoolVirtualMachineCount(2)
                .parent()
                .apply();

            System.out.println("Scaled Kubernetes cluster agent pool to 2 nodes.");
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageKubernetesCluster() {
    }
}
