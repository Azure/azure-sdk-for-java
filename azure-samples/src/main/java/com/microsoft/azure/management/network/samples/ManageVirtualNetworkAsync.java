/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Azure Network sample for managing virtual networks.
 *  - Create a virtual network with Subnets
 *  - Update a virtual network
 *  - Create virtual machines in the virtual network subnets
 *  - Create another virtual network
 *  - List virtual networks
 */

public final class ManageVirtualNetworkAsync {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        final String vnetName1 = SdkContext.randomResourceName("vnet1", 20);
        final String vnetName2 = SdkContext.randomResourceName("vnet2", 20);
        final String vnet1FrontEndSubnetName = "frontend";
        final String vnet1BackEndSubnetName = "backend";
        final String vnet1FrontEndSubnetNsgName = "frontendnsg";
        final String vnet1BackEndSubnetNsgName = "backendnsg";
        final String frontEndVMName = SdkContext.randomResourceName("fevm", 24);
        final String backEndVMName = SdkContext.randomResourceName("bevm", 24);
        final String publicIPAddressLeafDnsForFrontEndVM = SdkContext.randomResourceName("pip1", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        final String rgName = SdkContext.randomResourceName("rgNEMV", 24);

        try {
            //============================================================
            // Create a virtual network with specific address-space and two subnet

            // Creates a network security group for backend subnet
            System.out.println("Creating a network security group for virtual network backend subnet...");

            // Creates a network security group for frontend subnet
            System.out.println("Creating a network security group for virtual network backend subnet...");

            final Map<String, Indexable> createdResources = new TreeMap<>();

            Observable.merge(
                    azure.networkSecurityGroups().define(vnet1BackEndSubnetNsgName)
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
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
                        .createAsync()
                    .flatMap(new Func1<Indexable, Observable<Indexable>>() {
                        @Override
                        public Observable<Indexable> call(Indexable indexable) {
                            if (indexable instanceof NetworkSecurityGroup) {
                                NetworkSecurityGroup backEndNsg = (NetworkSecurityGroup) indexable;
                                System.out.println("Creating virtual network #1...");
                                return Observable.merge(
                                        Observable.just(indexable),
                                        azure.networks().define(vnetName1)
                                            .withRegion(Region.US_EAST)
                                            .withExistingResourceGroup(rgName)
                                            .withAddressSpace("192.168.0.0/16")
                                            .withSubnet(vnet1FrontEndSubnetName, "192.168.1.0/24")
                                            .defineSubnet(vnet1BackEndSubnetName)
                                                .withAddressPrefix("192.168.2.0/24")
                                                .withExistingNetworkSecurityGroup(backEndNsg)
                                                .attach()
                                            .createAsync());
                            }
                            return Observable.just(indexable);
                        }
                    }),
                    azure.networkSecurityGroups().define(vnet1FrontEndSubnetNsgName)
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(rgName)
                            .defineRule("AllowHttpInComing")
                                .allowInbound()
                                .fromAddress("INTERNET")
                                .fromAnyPort()
                                .toAnyAddress()
                                .toPort(80)
                                .withProtocol(SecurityRuleProtocol.TCP)
                                .attach()
                            .defineRule("DenyInternetOutGoing")
                                .denyOutbound()
                                .fromAnyAddress()
                                .fromAnyPort()
                                .toAddress("INTERNET")
                                .toAnyPort()
                                .withAnyProtocol()
                                .attach()
                            .createAsync()
                ).map(new Func1<Indexable, Indexable>() {
                @Override
                public Indexable call(Indexable indexable) {
                    if (indexable instanceof NetworkSecurityGroup) {
                        NetworkSecurityGroup nsg = (NetworkSecurityGroup) indexable;
                        System.out.println("Created network security group");
                        // Print the network security group
                        Utils.print(nsg);
                        createdResources.put(nsg.name(), nsg);
                    } else if (indexable instanceof Network) {
                        Network vn = (Network) indexable;
                        System.out.println("Created a virtual network");
                        // Print the virtual network details
                        Utils.print(vn);
                        createdResources.put(vn.name(), vn);
                    }
                    return indexable;
                }
            }).toBlocking().subscribe();

            NetworkSecurityGroup frontEndSubnetNsg = (NetworkSecurityGroup) createdResources.get(vnet1FrontEndSubnetNsgName);
            Network virtualNetwork1 = (Network) createdResources.get(vnetName1);


            //============================================================
            // Update a virtual network

            // Update the virtual network frontend subnet by associating it with network security group

            System.out.println("Associating network security group rule to frontend subnet");

            virtualNetwork1.update()
                    .updateSubnet(vnet1FrontEndSubnetName)
                        .withExistingNetworkSecurityGroup(frontEndSubnetNsg)
                        .parent()
                    .applyAsync()
            .toCompletable().await();

            System.out.println("Network security group rule associated with the frontend subnet");
            // Print the virtual network details
            Utils.print(virtualNetwork1);


            //============================================================
            // Create a virtual machine in each subnet and another virtual network
            // Creates the first virtual machine in frontend subnet
            System.out.println("Creating a Linux virtual machine in the frontend subnet");
            // Creates the second virtual machine in the backend subnet
            System.out.println("Creating a Linux virtual machine in the backend subnet");
            // Create a virtual network with default address-space and one default subnet
            System.out.println("Creating virtual network #2...");

            final Date t1 = new Date();

            Observable.merge(
                    azure.virtualMachines().define(frontEndVMName)
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(virtualNetwork1)
                            .withSubnet(vnet1FrontEndSubnetName)
                            .withPrimaryPrivateIPAddressDynamic()
                            .withNewPrimaryPublicIPAddress(publicIPAddressLeafDnsForFrontEndVM)
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .createAsync(),
                    azure.virtualMachines().define(backEndVMName)
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(virtualNetwork1)
                            .withSubnet(vnet1BackEndSubnetName)
                            .withPrimaryPrivateIPAddressDynamic()
                            .withoutPrimaryPublicIPAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .createAsync(),
                    azure.networks().define(vnetName2)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
                            .createAsync())
            .map(new Func1<Indexable, Indexable>() {
                @Override
                public Indexable call(Indexable indexable) {
                    Date t2 = new Date();
                    long duration = ((t2.getTime() - t1.getTime()) / 1000);

                    if (indexable instanceof VirtualMachine) {
                        VirtualMachine vm = (VirtualMachine) indexable;
                        System.out.println("Created Linux VM: (took "
                                + duration + " seconds) " + vm.id());
                        // Print virtual machine details
                        Utils.print(vm);
                    } else if (indexable instanceof Network) {
                        Network vn = (Network) indexable;
                        System.out.println("Created a virtual network: took "
                                + duration + " seconds) " + vn.id());
                        // Print the virtual network details
                        Utils.print(vn);
                    }
                    return indexable;
                }
            });

            //============================================================
            // List virtual networks and print details
            azure.networks().listByResourceGroupAsync(rgName)
                    .map(new Func1<Network, Network>() {
                        @Override
                        public Network call(Network network) {
                            Utils.print(network);
                            return network;
                        }
                    }).toBlocking().subscribe();

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName)
                        .await();
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
                    .withLogLevel(LogLevel.BODY)
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

    private ManageVirtualNetworkAsync() {
    }
}
