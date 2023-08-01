// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.Date;

/**
 * Azure Network sample for managing virtual networks -
 *  - Create a virtual network with Subnets
 *  - Update a virtual network
 *  - Create virtual machines in the virtual network subnets
 *  - Create another virtual network
 *  - List virtual networks
 *  - Delete a virtual network.
 */

public final class ManageVirtualNetwork {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String vnetName1 = Utils.randomResourceName(azureResourceManager, "vnet1", 20);
        final String vnetName2 = Utils.randomResourceName(azureResourceManager, "vnet2", 20);
        final String vnet1FrontEndSubnetName = "frontend";
        final String vnet1BackEndSubnetName = "backend";
        final String vnet1FrontEndSubnetNsgName = "frontendnsg";
        final String vnet1BackEndSubnetNsgName = "backendnsg";
        final String frontEndVMName = Utils.randomResourceName(azureResourceManager, "fevm", 24);
        final String backEndVMName = Utils.randomResourceName(azureResourceManager, "bevm", 24);
        final String publicIPAddressLeafDnsForFrontEndVM = Utils.randomResourceName(azureResourceManager, "pip1", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV", 24);

        try {
            //============================================================
            // Create a virtual network with specific address-space and two subnet

            // Creates a network security group for backend subnet

            System.out.println("Creating a network security group for virtual network backend subnet...");

            NetworkSecurityGroup backEndSubnetNsg = azureResourceManager.networkSecurityGroups().define(vnet1BackEndSubnetNsgName)
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
                    .create();

            System.out.println("Created network security group");
            // Print the network security group
            Utils.print(backEndSubnetNsg);

            // Create the virtual network with frontend and backend subnets, with
            // network security group rule applied to backend subnet]

            System.out.println("Creating virtual network #1...");

            Network virtualNetwork1 = azureResourceManager.networks().define(vnetName1)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace("192.168.0.0/16")
                    .withSubnet(vnet1FrontEndSubnetName, "192.168.1.0/24")
                    .defineSubnet(vnet1BackEndSubnetName)
                        .withAddressPrefix("192.168.2.0/24")
                        .withExistingNetworkSecurityGroup(backEndSubnetNsg)
                        .attach()
                    .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork1);


            //============================================================
            // Update a virtual network

            // Creates a network security group for frontend subnet

            System.out.println("Creating a network security group for virtual network backend subnet...");

            NetworkSecurityGroup frontEndSubnetNsg = azureResourceManager.networkSecurityGroups().define(vnet1FrontEndSubnetNsgName)
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
                    .create();

            System.out.println("Created network security group");
            // Print the network security group
            Utils.print(frontEndSubnetNsg);

            // Update the virtual network frontend subnet by associating it with network security group

            System.out.println("Associating network security group rule to frontend subnet");

            virtualNetwork1.update()
                    .updateSubnet(vnet1FrontEndSubnetName)
                        .withExistingNetworkSecurityGroup(frontEndSubnetNsg)
                        .parent()
                    .apply();

            System.out.println("Network security group rule associated with the frontend subnet");
            // Print the virtual network details
            Utils.print(virtualNetwork1);


            //============================================================
            // Create a virtual machine in each subnet

            // Creates the first virtual machine in frontend subnet

            System.out.println("Creating a Linux virtual machine in the frontend subnet");

            Date t1 = new Date();

            VirtualMachine frontEndVM = azureResourceManager.virtualMachines().define(frontEndVMName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(virtualNetwork1)
                    .withSubnet(vnet1FrontEndSubnetName)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPAddressLeafDnsForFrontEndVM)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            Date t2 = new Date();
            System.out.println("Created Linux VM: (took "
                    + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + frontEndVM.id());
            // Print virtual machine details
            Utils.print(frontEndVM);


            // Creates the second virtual machine in the backend subnet

            System.out.println("Creating a Linux virtual machine in the backend subnet");

            Date t3 = new Date();

            VirtualMachine backEndVM = azureResourceManager.virtualMachines().define(backEndVMName)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(virtualNetwork1)
                    .withSubnet(vnet1BackEndSubnetName)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            Date t4 = new Date();
            System.out.println("Created Linux VM: (took "
                    + ((t4.getTime() - t3.getTime()) / 1000) + " seconds) " + backEndVM.id());
            // Print virtual machine details
            Utils.print(backEndVM);


            //============================================================
            // Create a virtual network with default address-space and one default subnet

            System.out.println("Creating virtual network #2...");

            Network virtualNetwork2 = azureResourceManager.networks().define(vnetName2)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork2);


            //============================================================
            // List virtual networks

            for (Network virtualNetwork : azureResourceManager.networks().listByResourceGroup(rgName)) {
                Utils.print(virtualNetwork);
            }


            //============================================================
            // Delete a virtual network
            System.out.println("Deleting the virtual network");
            azureResourceManager.networks().deleteById(virtualNetwork2.id());
            System.out.println("Deleted the virtual network");

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

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

    private ManageVirtualNetwork() {
    }
}
