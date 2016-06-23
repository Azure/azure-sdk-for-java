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
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.Date;

/**
 * Azure Network sample for managing network security groups -
 *  - Create a network security group for the front end of a subnet
 *  - Create a network security group fro the back end of a subnet
 *  - List network security groups
 *  - Update a network security group
 *  - Delete a network security group.
 */

public final class ManageNetworkSecurityGroup {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String frontEndNSGName = ResourceNamer.randomResourceName("fensg", 24);
        final String backEndNSGName = ResourceNamer.randomResourceName("bensg", 24);
        final String rgName = ResourceNamer.randomResourceName("rgNEMS", 24);
        final String vnetName = ResourceNamer.randomResourceName("vnet", 24);
        final String networkInterfaceName1 = ResourceNamer.randomResourceName("nic1", 24);
        final String networkInterfaceName2 = ResourceNamer.randomResourceName("nic2", 24);
        final String publicIpAddressLeafDNS1 = ResourceNamer.randomResourceName("pip1", 24);
        final String vmName = ResourceNamer.randomResourceName("vm", 24);
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

                // Define a virtual network for VMs in this availability set

                System.out.println("Creating a virtual network ...");

                Network network = azure.networks()
                        .define(vnetName)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .withAddressSpace("172.16.0.0/16")
                        .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .attach()
                        .defineSubnet("Back-end")
                        .withAddressPrefix("172.16.2.0/24")
                        .attach()
                        .create();


                //============================================================
                // Create a network security group for the front end of a subnet
                // front end subnet contains two rules
                // - ALLOW-SSH - allows SSH traffic into the front end subnet
                // - ALLOW-WEB- allows HTTP traffic into the front end subnet

                System.out.println("Creating a security group for the front end - allows SSH and Web");
                NetworkSecurityGroup frontEndNSG = azure.networkSecurityGroups().define(frontEndNSGName)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .defineRule("ALLOW-SSH")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(22)
                        .withProtocol(NetworkSecurityRule.Protocol.TCP)
                        .withPriority(100)
                        .withDescription("Allow SSH")
                        .attach()
                        .defineRule("ALLOW-HTTP")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(80)
                        .withProtocol(NetworkSecurityRule.Protocol.TCP)
                        .withPriority(101)
                        .withDescription("Allow HTTP")
                        .attach()
                        .create();

                System.out.println("Created a security group for the front end: " + frontEndNSG.id());
                Utils.print(frontEndNSG);


                //============================================================
                // Create a network security group for the back end of a subnet
                // back end subnet contains two rules
                // - ALLOW-SQL - allows SQL traffic only from the front end subnet
                // - DENY-WEB - denies all internet bound traffic from the back end subnet

                System.out.println("Creating a security group for the front end - allows SSH and Web");
                NetworkSecurityGroup backEndNSG = azure.networkSecurityGroups().define(backEndNSGName)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .defineRule("ALLOW-SQL")
                        .allowInbound()
                        .fromAddress("172.16.1.0/24")
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(1433)
                        .withProtocol(NetworkSecurityRule.Protocol.TCP)
                        .withPriority(100)
                        .withDescription("Allow SQL")
                        .attach()
                        .defineRule("DENY-WEB")
                        .denyOutbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toAnyPort()
                        .withAnyProtocol()
                        .withDescription("Deny Web")
                        .withPriority(200)
                        .attach()
                        .create();

                System.out.println("Created a security group for the back end: " + backEndNSG.id());
                Utils.print(backEndNSG);


                System.out.println("Created a virtual network: " + network.id());
                Utils.print(network);

                System.out.println("Creating multiple network interfaces");
                System.out.println("Creating network interface 1");

                NetworkInterface networkInterface1 = azure.networkInterfaces().define(networkInterfaceName1)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("Front-end")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withNewPrimaryPublicIpAddress(publicIpAddressLeafDNS1)
                        .withIpForwarding()
                        .create();

                System.out.println("Created network interface 1");
                Utils.print(networkInterface1);


                System.out.println("Applying front end network security group to network interface 1");
                networkInterface1.update()
                        .withExistingNetworkSecurityGroup(frontEndNSG)
                        .apply();
                System.out.println("Applied front end network security group to network interface 1");


                System.out.println("Creating network interface 2");
                NetworkInterface networkInterface2 = azure.networkInterfaces().define(networkInterfaceName2)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("Back-end")
                        .withPrimaryPrivateIpAddressDynamic()
                        .create();

                System.out.println("Created network interface 2");
                Utils.print(networkInterface2);

                System.out.println("Applying front end network security group to network interface 2");
                networkInterface2.update()
                        .withExistingNetworkSecurityGroup(backEndNSG)
                        .apply();
                System.out.println("Applied front end network security group to network interface 2");

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


                // List network security groups

                // Attach network security groups to a VM

                // Update a network security group

                // Delete a network security group

            } catch (Exception f) {

                System.out.println(f.getMessage());
                f.printStackTrace();

            } finally {

                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }
            }
        } catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
        }
    }

    private ManageNetworkSecurityGroup() {

    }
}