/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Azure Network sample for managing virtual networks -
 *  - Create a virtual network
 *  - Create a virtual network with Subnets
 *  - Update a virtual network
 *  - Create another virtual network
 *  - List virtual networks
 *  - Delete a virtual network.
 */

public final class ManageVirtualNetwork {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        final String vnetName1 = ResourceNamer.randomResourceName("vnet1", 20);
        final String vnetName2 = ResourceNamer.randomResourceName("vnet2", 20);
        final String vnet2FrontEndSubnetName = "frontend";
        final String vnet2BackEndSubnetName = "backend";
        final String vnet2FrontEndSubnetNsgName = "frontendnsg";
        final String vnet2BackEndSubnetNsgName = "backendnsg";

        final String rgName = ResourceNamer.randomResourceName("rgNEMVnet", 24);
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File("my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {

                //============================================================
                // Create a virtual network with default address-space and one default subnet

                System.out.println("Creating virtual network #1...");

                Network virtualNetwork1 = azure.networks()
                        .define(vnetName1)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .create();

                // Print the virtual network details
                Utils.print(virtualNetwork1);

                //============================================================
                // Create a virtual network with specific address-space and two subnet

                // Creates a network security group for backend subnet

                System.out.println("Creating a network security group for virtual network backend subnet...");

                NetworkSecurityGroup backEndSubnetNsg = azure.networkSecurityGroups()
                        .define(vnet2BackEndSubnetNsgName)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .defineRule("DenyInternetInComing")
                            .denyInbound()
                            .fromAddress("INTERNET")
                            .fromAnyPort()
                            .toAnyAddress()
                            .toAnyPort()
                            .withAnyProtocol()
                            .attach()
                        .defineRule("DenyInternetOutGoing")
                            .denyOutbound()
                            .fromAnyAddress()
                            .fromAnyPort()
                            .toAddress("INTERNET")
                            .toAnyPort()
                            .withAnyProtocol()
                            .attach()
                        .create();

                // Create the virtual network

                System.out.println("Creating virtual network #2...");

                Network virtualNetwork2 = azure.networks()
                        .define(vnetName2)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .withAddressSpace("192.168.0.0/16")
                        .withSubnet(vnet2FrontEndSubnetName, "192.168.1.0/24")
                        .defineSubnet(vnet2BackEndSubnetName)
                            .withAddressPrefix("192.168.2.0/24")
                            .withExistingNetworkSecurityGroup(backEndSubnetNsg)
                            .attach()
                        .create();

                // Print the virtual network details
                Utils.print(virtualNetwork2);

                //============================================================
                // Update a virtual network

                // Creates a network security group for frontend subnet

                System.out.println("Creating a network security group for virtual network backend subnet...");

                NetworkSecurityGroup frontEndSubnetNsg = azure.networkSecurityGroups()
                        .define(vnet2FrontEndSubnetNsgName)
                        .withRegion(Region.US_EAST)
                        .withExistingGroup(rgName)
                        .defineRule("AllowHttpInComing")
                            .allowInbound()
                            .fromAddress("INTERNET")
                            .fromAnyPort()
                            .toAnyAddress()
                            .toPort(80)
                            .withProtocol(NetworkSecurityRule.Protocol.TCP)
                            .attach()
                        .defineRule("DenyInternetOutGoing")
                            .denyOutbound()
                            .fromAnyAddress()
                            .fromAnyPort()
                            .toAddress("INTERNET")
                            .toAnyPort()
                            .withAnyProtocol()
                            .attach()
                        .create();

                // Update the virtual network front end subnet

                virtualNetwork2.update()
                        .updateSubnet(vnet2FrontEndSubnetName)
                            .withExistingNetworkSecurityGroup(frontEndSubnetNsg)
                            .parent()
                        .apply();

                // Print the virtual network details
                Utils.print(virtualNetwork2);

                //============================================================
                // List virtual networks

                for (Network virtualNetwork : azure.networks().listByGroup(rgName)) {
                    Utils.print(virtualNetwork);
                }

                //============================================================
                // Delete a virtual network
                azure.networks().delete(virtualNetwork1.id());
            } catch (Exception e) {
                System.err.println(e.getMessage());
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

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVirtualNetwork() {
    }
}

