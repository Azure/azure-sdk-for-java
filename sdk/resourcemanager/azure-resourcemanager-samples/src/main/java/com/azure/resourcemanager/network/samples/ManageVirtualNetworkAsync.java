// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Azure Network sample for managing virtual networks.
 * - Create a virtual network with Subnets
 * - Update a virtual network
 * - Create virtual machines in the virtual network subnets
 * - Create another virtual network
 * - List virtual networks
 */

public final class ManageVirtualNetworkAsync {

    private static class Indexable2Duration {
        Indexable indexable;
        Long duration;

        Indexable2Duration(Indexable indexable, long duration) {
            this.indexable = indexable;
            this.duration = duration;
        }
    }

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        final String vnetName1 = azure.sdkContext().randomResourceName("vnet1", 20);
        final String vnetName2 = azure.sdkContext().randomResourceName("vnet2", 20);
        final String vnet1FrontEndSubnetName = "frontend";
        final String vnet1BackEndSubnetName = "backend";
        final String vnet1FrontEndSubnetNsgName = "frontendnsg";
        final String vnet1BackEndSubnetNsgName = "backendnsg";
        final String frontEndVMName = azure.sdkContext().randomResourceName("fevm", 24);
        final String backEndVMName = azure.sdkContext().randomResourceName("bevm", 24);
        final String publicIPAddressLeafDnsForFrontEndVM = azure.sdkContext().randomResourceName("pip1", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        final String rgName = azure.sdkContext().randomResourceName("rgNEMV", 24);

        try {
            //============================================================
            // Create a virtual network with specific address-space and two subnet

            // Creates a network security group for backend subnet
            System.out.println("Creating a network security group for virtual network backend subnet...");

            // Creates a network security group for frontend subnet
            System.out.println("Creating a network security group for virtual network backend subnet...");

            final Map<String, Indexable> createdResources = new TreeMap<>();

            Flux.merge(
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
                            .flatMap(indexable -> {
                                if (indexable instanceof NetworkSecurityGroup) {
                                    NetworkSecurityGroup backEndNsg = (NetworkSecurityGroup) indexable;
                                    System.out.println("Creating virtual network #1...");
                                    return Flux.merge(
                                            Flux.just(indexable),
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
                                return Flux.just(indexable);
                            }),
                    azure.networkSecurityGroups().define(vnet1FrontEndSubnetNsgName)
                            .withRegion(Region.US_EAST)
                            .withNewResourceGroup(rgName)
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
            ).map(indexable -> {
                if (indexable instanceof NetworkSecurityGroup) {
                    NetworkSecurityGroup nsg = (NetworkSecurityGroup) indexable;
                    createdResources.put(nsg.name(), nsg);
                } else if (indexable instanceof Network) {
                    Network vn = (Network) indexable;
                    createdResources.put(vn.name(), vn);
                }
                return indexable;
            }).blockLast();

            NetworkSecurityGroup frontEndSubnetNsg = (NetworkSecurityGroup) createdResources.get(vnet1FrontEndSubnetNsgName);
            Network virtualNetwork1 = (Network) createdResources.get(vnetName1);

            System.out.println("Created network security group");
            // Print the network security group
            Utils.print(frontEndSubnetNsg);

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork1);

            //============================================================
            // Update a virtual network

            // Update the virtual network frontend subnet by associating it with network security group

            System.out.println("Associating network security group rule to frontend subnet");

            virtualNetwork1.update()
                    .updateSubnet(vnet1FrontEndSubnetName)
                    .withExistingNetworkSecurityGroup(frontEndSubnetNsg)
                    .parent()
                    .applyAsync()
                    .block();

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

            final List<Indexable2Duration> creations = new ArrayList<>();
            final Date t1 = new Date();

            Flux.merge(
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
                    .map(indexable -> {
                        Date t2 = new Date();
                        long duration = ((t2.getTime() - t1.getTime()) / 1000);
                        creations.add(new Indexable2Duration(indexable, duration));
                        return indexable;
                    });

            for (Indexable2Duration creation : creations) {
                if (creation.indexable instanceof VirtualMachine) {
                    VirtualMachine vm = (VirtualMachine) creation.indexable;
                    System.out.println("Created Linux VM: (took "
                            + creation.duration + " seconds) " + vm.id());
                    // Print virtual machine details
                    Utils.print(vm);
                } else if (creation.indexable instanceof Network) {
                    Network vn = (Network) creation.indexable;
                    System.out.println("Created a virtual network: took "
                            + creation.duration + " seconds) " + vn.id());
                    // Print the virtual network details
                    Utils.print(vn);
                }
            }

            //============================================================
            // List virtual networks and print details
            for (Network network : azure.networks().listByResourceGroup(rgName)) {
                Utils.print(network);
            }

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName).block();
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
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
