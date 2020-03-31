/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.management.Azure;
import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.ServiceEndpointType;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;
import com.azure.management.storage.StorageAccount;

import java.io.File;

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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgSTMS", 8);
        final String networkName = azure.sdkContext().randomResourceName("nw", 8);
        final String subnetName = "subnetA";
        final String storageAccountName = azure.sdkContext().randomResourceName("sa", 8);
        final String publicIpName = azure.sdkContext().randomResourceName("pip", 8);
        final String vmName = azure.sdkContext().randomResourceName("vm", 8);

        try {
            // ============================================================
            // Create a virtual network and a subnet with storage service subnet access enabled

            System.out.println("Creating a Virtual network and subnet with storage service subnet access enabled:");

            final Network network = azure.networks().define(networkName)
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

            StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
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

            final PublicIPAddress publicIPAddress = azure.publicIPAddresses()
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

            VirtualMachine linuxVM = azure.virtualMachines()
                    .define(vmName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.1.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIPAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withRootPassword("12NewPA$$w0rd!")
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
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

    private ManageStorageAccountNetworkRules() {
    }
}
