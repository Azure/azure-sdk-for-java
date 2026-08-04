// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.SampleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Azure Compute sample for creating multiple virtual machines in parallel in the same virtual network.
 */
public final class ManageVirtualMachinesInParallel {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final int vmCount = 5;
        final Region region = Region.US_SOUTH_CENTRAL;
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgCOPP", 24);
        final String networkName = SampleUtils.randomResourceName(azureResourceManager, "vnetCOMV", 24);
        final String userName = "tirekicker";
        final String sshPublicKey = SampleUtils.sshPublicKey();
        try {
            ResourceGroup resourceGroup
                = azureResourceManager.resourceGroups().define(rgName).withRegion(region).create();

            // Shared virtual network that all the virtual machines are attached to.
            Creatable<Network> creatableNetwork = azureResourceManager.networks()
                .define(networkName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("172.16.0.0/16");

            // Prepare the batch of virtual machine definitions.
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();
            for (int i = 0; i < vmCount; i++) {
                creatableVirtualMachines.add(azureResourceManager.virtualMachines()
                    .define("VM-" + i)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetwork(creatableNetwork)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D2_V3));
            }

            // Create all virtual machines in a single parallel operation.
            Collection<VirtualMachine> virtualMachines
                = azureResourceManager.virtualMachines().create(creatableVirtualMachines).values();

            System.out.println("Created " + virtualMachines.size() + " virtual machines:");
            for (VirtualMachine virtualMachine : virtualMachines) {
                System.out.println("\t" + virtualMachine.id());
            }
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

    private ManageVirtualMachinesInParallel() {
    }
}
