// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.Date;

/**
 * Azure Network sample for managing network interfaces -
 * - Create a virtual machine with multiple network interfaces
 * - Configure a network interface
 * - List network interfaces
 * - Delete a network interface.
 */

public final class ManageNetworkInterface {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST;
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 24);
        final String networkInterfaceName1 = Utils.randomResourceName(azureResourceManager, "nic1", 24);
        final String networkInterfaceName2 = Utils.randomResourceName(azureResourceManager, "nic2", 24);
        final String networkInterfaceName3 = Utils.randomResourceName(azureResourceManager, "nic3", 24);
        final String publicIPAddressLeafDNS1 = Utils.randomResourceName(azureResourceManager, "pip1", 24);
        final String publicIPAddressLeafDNS2 = Utils.randomResourceName(azureResourceManager, "pip2", 24);

        // TODO: adjust the length of vm name from 8 to 24
        final String vmName = Utils.randomResourceName(azureResourceManager, "vm", 8);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMI", 24);
        final String userName = "tirekicker";
        final String password = Utils.password();
        try {

            //============================================================
            // Create a virtual machine with multiple network interfaces

            // Define a virtual network for the VMs in this availability set

            System.out.println("Creating a virtual network ...");

            Network network = azureResourceManager.networks().define(vnetName)
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

            NetworkInterface networkInterface1 = azureResourceManager.networkInterfaces().define(networkInterfaceName1)
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

            NetworkInterface networkInterface2 = azureResourceManager.networkInterfaces().define(networkInterfaceName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Mid-tier")
                    .withPrimaryPrivateIPAddressDynamic()
                    .create();

            System.out.println("Created network interface 2");
            Utils.print(networkInterface2);

            System.out.println("Creating network interface 3");

            NetworkInterface networkInterface3 = azureResourceManager.networkInterfaces().define(networkInterfaceName3)
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

            VirtualMachine vm = azureResourceManager.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetworkInterface(networkInterface1)
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(userName)
                    .withAdminPassword(password)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D8a_v4"))
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
            PagedIterable<NetworkInterface> networkInterfaces = azureResourceManager.networkInterfaces().listByResourceGroup(rgName);
            for (NetworkInterface networkinterface : networkInterfaces) {
                Utils.print(networkinterface);
            }


            //============================================================
            // Delete a network interface

            System.out.println("Deleting a network interface: " + networkInterface2.id());
            System.out.println("First, deleting the vm");
            azureResourceManager.virtualMachines().deleteById(vm.id());
            System.out.println("Second, deleting the network interface");
            azureResourceManager.networkInterfaces().deleteById(networkInterface2.id());
            System.out.println("Deleted network interface");

            System.out.println("============================================================");
            System.out.println("Remaining network interfaces are ...");
            networkInterfaces = azureResourceManager.networkInterfaces().listByResourceGroup(rgName);
            for (NetworkInterface networkinterface : networkInterfaces) {
                Utils.print(networkinterface);
            }
            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
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

    private ManageNetworkInterface() {

    }
}
