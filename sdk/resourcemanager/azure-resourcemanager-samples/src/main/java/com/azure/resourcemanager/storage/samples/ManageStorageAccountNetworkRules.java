// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;

/**
 * Azure Storage sample for managing storage account network rules -
 * - Create a virtual network and subnet with storage service subnet access enabled
 * - Create a storage account with access allowed only from the subnet
 * - Create a public IP address
 * - Create a virtual machine and associate the public IP address
 * - Update the storage account with access also allowed from the public IP address
 * - Update the storage account to restrict incoming traffic to HTTPS.
 */
public final class ManageStorageAccountNetworkRules {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSTMS", 8);
        final String networkName = Utils.randomResourceName(azureResourceManager, "nw", 8);
        final String subnetName = "subnetA";
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "sa", 8);
        final String publicIpName = Utils.randomResourceName(azureResourceManager, "pip", 8);
        final String vmName = Utils.randomResourceName(azureResourceManager, "vm", 8);

        try {
            // ============================================================
            // Create a virtual network and a subnet with storage service subnet access enabled

            System.out.println("Creating a Virtual network and subnet with storage service subnet access enabled:");

            final Network network = azureResourceManager.networks().define(networkName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.0.0.0/28")
                    .defineSubnet(subnetName)
                    .withAddressPrefix("10.0.0.8/29")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_STORAGE)
                    .attach()
                    .create();

            System.out.println("Created a Virtual network with subnet:");
            Utils.print(network);

            // ============================================================
            // Create a storage account with access to it allowed only from a specific subnet

            final String subnetId = String.format("%s/subnets/%s", network.id(), subnetName);

            System.out.println("Creating a storage account with access allowed only from the subnet :" + subnetId);

            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withAccessFromSelectedNetworks()
                    .withAccessFromNetworkSubnet(subnetId)
                    .create();

            System.out.println("Created storage account:");
            Utils.print(storageAccount);

            // ============================================================
            // Create a public IP address

            System.out.println("Creating a Public IP address");

            final PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses()
                    .define(publicIpName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpName)
                    .create();

            System.out.println("Created Public IP address:");
            Utils.print(publicIPAddress);

            // ============================================================
            // Create a virtual machine and associate the public IP address

            System.out.println("Creating a VM with the Public IP address");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines()
                    .define(vmName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.1.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIPAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withRootPassword(Utils.password())
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created the VM: " + linuxVM.id());
            Utils.print(linuxVM);

            publicIPAddress.refresh();  // Refresh public IP resource to populate the assigned IPv4 address

            // ============================================================
            // Update the storage account so that it can also be accessed from the PublicIP address

            System.out.println("Updating storage account with access also allowed from publicIP :" + publicIPAddress.ipAddress());

            storageAccount.update()
                    .withAccessFromIpAddress(publicIPAddress.ipAddress())
                    .apply();

            System.out.println("Updated storage account:");
            Utils.print(storageAccount);

            // ============================================================
            //  Update the storage account to restrict incoming traffic to HTTPS

            System.out.println("Restricting access to storage account only via HTTPS");

            storageAccount.update()
                    .withOnlyHttpsTraffic()
                    .apply();

            System.out.println("Updated the storage account:");
            Utils.print(storageAccount);

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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

    private ManageStorageAccountNetworkRules() {
    }
}
