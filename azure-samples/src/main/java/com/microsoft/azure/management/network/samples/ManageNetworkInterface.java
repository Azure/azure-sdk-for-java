/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.Date;

/**
 * Azure Network sample for managing network interfaces -
 *  - Create a virtual machine with multiple network interfaces
 *  - Configure multiple network interfaces
 *  - List network interfaces
 *  - Delete a network interface.
 */

public final class ManageNetworkInterface {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String vnetName = ResourceNamer.randomResourceName("vnet", 24);
        final String networkInterfaceName1 = ResourceNamer.randomResourceName("nic1", 24);
        final String networkInterfaceName2 = ResourceNamer.randomResourceName("nic2", 24);
        final String publicIpAddressLeafDNS1 = ResourceNamer.randomResourceName("pip1", 24);
        final String publicIpAddressLeafDNS2 = ResourceNamer.randomResourceName("pip2", 24);

        final String vmName = ResourceNamer.randomResourceName("vm", 24);
        final String rgName = ResourceNamer.randomResourceName("rgNEMI", 24);
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File("my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {

                //============================================================
                // Create a virtual machine with multiple network interfaces

                // Define a virtual network for the VMs in this availability set
                Network.DefinitionCreatable network = azure.networks()
                        .define(vnetName)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .withAddressSpace("10.0.0.0/28");

                System.out.println("Creating multiple network interfaces");
                NetworkInterface networkInterface1 = azure.networkInterfaces().define(networkInterfaceName1)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .withNewPrimaryNetwork(network)
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(publicIpAddressLeafDNS1)
                        .withIpForwarding()
                        .create();

                NetworkInterface networkInterface2 = azure.networkInterfaces().define(networkInterfaceName2)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .withNewPrimaryNetwork(network)
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(publicIpAddressLeafDNS2)
                        .withIpForwarding()
                        .create();

                System.out.println("Created 2 network interfaces:");
                Utils.print(networkInterface1);
                Utils.print(networkInterface2);


                //=============================================================
                // Create a virtual machine with multiple network interfaces

                System.out.println("Creating a Windows VM");

                Date t1 = new Date();

                VirtualMachine vm = azure.virtualMachines().define(vmName)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .withExistingPrimaryNetworkInterface(networkInterface1)
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUserName(userName)
                        .withPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

                Date t2 = new Date();
                System.out.println("Created VM: (took "
                        + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + vm.id());
                // Print virtual machine details
                Utils.print(vm);

                // add a secondary network interface
                System.out.println("Adding a secondary network interface");
                vm.update()
                        .withExistingSecondaryNetworkInterface(networkInterface2)
                        .apply();

                System.out.println("Added a secondary network interface");
                // Print virtual machine details
                Utils.print(vm);


                // ===========================================================
                // Configure multiple network interfaces
                System.out.println("Updating the first network interface");
                networkInterface1.update()
                        .withAzureDnsServer()
                        .apply();

                System.out.println("Updated the first network interface");
                Utils.print(networkInterface1);
                System.out.println();


                //============================================================
                // List network interfaces

                System.out.println("Walking through network inter4faces");
                NetworkInterfaces networkInterfaces = azure.networkInterfaces();
                for (NetworkInterface networkinterface : networkInterfaces.list()) {
                    Utils.print(networkinterface);
                }


                //============================================================
                // Delete a network interface

                System.out.println("Deleting a network interface: " + networkInterface2.id());
                azure.networkInterfaces().delete(networkInterface2.id());
                System.out.println("Deleted network interface");

                System.out.println("Remaining network interfaces are ...");
                networkInterfaces = azure.networkInterfaces();
                for (NetworkInterface networkinterface : networkInterfaces.list()) {
                    Utils.print(networkinterface);
                }
            } catch (Exception f) {

                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }

                System.out.println(f.getMessage());
                f.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageNetworkInterface() {

    }
}
