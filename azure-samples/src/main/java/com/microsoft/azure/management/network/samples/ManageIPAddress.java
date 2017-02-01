/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Date;

/**
 * Azure Network sample for managing IP address -
 *  - Assign a public IP address for a virtual machine during its creation
 *  - Assign a public IP address for a virtual machine through an virtual machine update action
 *  - Get the associated public IP address for a virtual machine
 *  - Get the assigned public IP address for a virtual machine
 *  - Remove a public IP address from a virtual machine.
 */
public final class ManageIPAddress {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String publicIpAddressName1 = SdkContext.randomResourceName("pip1", 20);
        final String publicIpAddressName2 = SdkContext.randomResourceName("pip2", 20);
        final String publicIpAddressLeafDNS1 = SdkContext.randomResourceName("pip1", 20);
        final String publicIpAddressLeafDNS2 = SdkContext.randomResourceName("pip2", 20);
        final String vmName = SdkContext.randomResourceName("vm", 8);
        final String rgName = SdkContext.randomResourceName("rgNEMP", 24);
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {

            //============================================================
            // Assign a public IP address for a VM during its creation

            // Define a public IP address to be used during VM creation time

            System.out.println("Creating a public IP address...");

            PublicIpAddress publicIpAddress = azure.publicIpAddresses().define(publicIpAddressName1)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpAddressLeafDNS1)
                    .create();

            System.out.println("Created a public IP address");

            // Print public IP address details
            Utils.print(publicIpAddress);

            // Use the pre-created public IP for the new VM

            System.out.println("Creating a Windows VM");

            Date t1 = new Date();

            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIpAddressDynamic()
                    .withExistingPrimaryPublicIpAddress(publicIpAddress)
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(userName)
                    .withAdminPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            Date t2 = new Date();
            System.out.println("Created VM: (took "
                    + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + vm.id());
            // Print virtual machine details
            Utils.print(vm);


            //============================================================
            // Gets the public IP address associated with the VM's primary NIC

            System.out.println("Public IP address associated with the VM's primary NIC [After create]");
            // Print the public IP address details
            Utils.print(vm.getPrimaryPublicIpAddress());


            //============================================================
            // Assign a new public IP address for the VM

            // Define a new public IP address

            PublicIpAddress publicIpAddress2 = azure.publicIpAddresses().define(publicIpAddressName2)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpAddressLeafDNS2)
                    .create();

            // Update VM's primary NIC to use the new public IP address

            System.out.println("Updating the VM's primary NIC with new public IP address");

            NetworkInterface primaryNetworkInterface = vm.getPrimaryNetworkInterface();
            primaryNetworkInterface.update()
                .withExistingPrimaryPublicIpAddress(publicIpAddress2)
                .apply();


            //============================================================
            // Gets the updated public IP address associated with the VM

            // Get the associated public IP address for a virtual machine
            System.out.println("Public IP address associated with the VM's primary NIC [After Update]");
            vm.refresh();
            Utils.print(vm.getPrimaryPublicIpAddress());


            //============================================================
            // Remove public IP associated with the VM

            System.out.println("Removing public IP address associated with the VM");
            vm.refresh();
            primaryNetworkInterface = vm.getPrimaryNetworkInterface();
            publicIpAddress = primaryNetworkInterface.primaryIpConfiguration().getPublicIpAddress();
            primaryNetworkInterface.update()
                .withoutPrimaryPublicIpAddress()
                .apply();

            System.out.println("Removed public IP address associated with the VM");


            //============================================================
            // Delete the public ip
            System.out.println("Deleting the public IP address");
            azure.publicIpAddresses().deleteById(publicIpAddress.id());
            System.out.println("Deleted the public IP address");
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
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
     * @param args the parameters
     */
    public static void main(String[] args) {


        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
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

    private ManageIPAddress() {
    }
}
