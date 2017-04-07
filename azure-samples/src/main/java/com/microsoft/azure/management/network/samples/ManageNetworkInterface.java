/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Date;

/**
 * Azure Network sample for managing network interfaces -
 *  - Create a virtual machine with multiple network interfaces
 *  - Configure a network interface
 *  - List network interfaces
 *  - Delete a network interface.
 */

public final class ManageNetworkInterface {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_NORTH_CENTRAL;
        final String vnetName = SdkContext.randomResourceName("vnet", 24);
        final String networkInterfaceName1 = SdkContext.randomResourceName("nic1", 24);
        final String networkInterfaceName2 = SdkContext.randomResourceName("nic2", 24);
        final String networkInterfaceName3 = SdkContext.randomResourceName("nic3", 24);
        final String publicIPAddressLeafDNS1 = SdkContext.randomResourceName("pip1", 24);
        final String publicIPAddressLeafDNS2 = SdkContext.randomResourceName("pip2", 24);

        // TODO adjust the length of vm name from 8 to 24
        final String vmName = SdkContext.randomResourceName("vm", 8);
        final String rgName = SdkContext.randomResourceName("rgNEMI", 24);
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        try {

            //============================================================
            // Create a virtual machine with multiple network interfaces

            // Define a virtual network for the VMs in this availability set

            System.out.println("Creating a virtual network ...");

            Network network = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("172.16.0.0/16")
                    .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .attach()
                    .defineSubnet("Mid-tier")
                        .withAddressPrefix("172.16.2.0/24")
                        .attach()
                    .defineSubnet("Back-end")
                        .withAddressPrefix("172.16.3.0/24")
                        .attach()
                    .create();

            System.out.println("Created a virtual network: " + network.id());
            Utils.print(network);

            System.out.println("Creating multiple network interfaces");
            System.out.println("Creating network interface 1");

            NetworkInterface networkInterface1 = azure.networkInterfaces().define(networkInterfaceName1)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Front-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPAddressLeafDNS1)
                    .withIPForwarding()
                    .create();

            System.out.println("Created network interface 1");
            Utils.print(networkInterface1);
            System.out.println("Creating network interface 2");

            NetworkInterface networkInterface2 = azure.networkInterfaces().define(networkInterfaceName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Mid-tier")
                    .withPrimaryPrivateIPAddressDynamic()
                    .create();

            System.out.println("Created network interface 2");
            Utils.print(networkInterface2);

            System.out.println("Creating network interface 3");

            NetworkInterface networkInterface3 = azure.networkInterfaces().define(networkInterfaceName3)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Back-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .create();

            System.out.println("Created network interface 3");
            Utils.print(networkInterface3);


            //=============================================================
            // Create a virtual machine with multiple network interfaces

            System.out.println("Creating a Windows VM");

            Date t1 = new Date();

            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetworkInterface(networkInterface1)
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(userName)
                    .withAdminPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .withExistingSecondaryNetworkInterface(networkInterface2)
                    .withExistingSecondaryNetworkInterface(networkInterface3)
                    .create();

            Date t2 = new Date();
            System.out.println("Created VM: (took "
                    + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + vm.id());
            // Print virtual machine details
            Utils.print(vm);


            // ===========================================================
            // Configure a network interface
            System.out.println("Updating the first network interface");
            networkInterface1.update()
                    .withNewPrimaryPublicIPAddress(publicIPAddressLeafDNS2)
                    .apply();

            System.out.println("Updated the first network interface");
            Utils.print(networkInterface1);
            System.out.println();


            //============================================================
            // List network interfaces

            System.out.println("Walking through network inter4faces in resource group: " + rgName);
            PagedList<NetworkInterface> networkInterfaces = azure.networkInterfaces().listByResourceGroup(rgName);
            for (NetworkInterface networkinterface : networkInterfaces) {
                Utils.print(networkinterface);
            }


            //============================================================
            // Delete a network interface

            System.out.println("Deleting a network interface: " + networkInterface2.id());
            System.out.println("First, deleting the vm");
            azure.virtualMachines().deleteById(vm.id());
            System.out.println("Second, deleting the network interface");
            azure.networkInterfaces().deleteById(networkInterface2.id());
            System.out.println("Deleted network interface");

            System.out.println("============================================================");
            System.out.println("Remaining network interfaces are ...");
            networkInterfaces = azure.networkInterfaces().listByResourceGroup(rgName);
            for (NetworkInterface networkinterface : networkInterfaces) {
                Utils.print(networkinterface);
            }
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

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

    private ManageNetworkInterface() {

    }
}
