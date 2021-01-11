// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;
import com.jcraft.jsch.JSchException;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Azure Network sample for managing network security groups -
 * - Create a network security group for the front end of a subnet
 * - Create a network security group for the back end of a subnet
 * - Create Linux virtual machines for the front end and back end
 * -- Apply network security groups
 * - List network security groups
 * - Update a network security group.
 */

public final class ManageNetworkSecurityGroup {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws UnsupportedEncodingException, JSchException {
        final Region region = Region.US_WEST;
        final String frontEndNSGName = Utils.randomResourceName(azureResourceManager, "fensg", 24);
        final String backEndNSGName = Utils.randomResourceName(azureResourceManager, "bensg", 24);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMS", 24);
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 24);
        final String networkInterfaceName1 = Utils.randomResourceName(azureResourceManager, "nic1", 24);
        final String networkInterfaceName2 = Utils.randomResourceName(azureResourceManager, "nic2", 24);
        final String publicIPAddressLeafDNS1 = Utils.randomResourceName(azureResourceManager, "pip1", 24);
        final String frontEndVMName = Utils.randomResourceName(azureResourceManager, "fevm", 24);
        final String backEndVMName = Utils.randomResourceName(azureResourceManager, "bevm", 24);
        final String userName = "tirekicker";
        try {
            final String sshKey = SSHShell.generateSSHKeys(null, null).getSshPublicKey();

            // Define a virtual network for VMs in this availability set

            System.out.println("Creating a virtual network ...");

            Network network = azureResourceManager.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("172.16.0.0/16")
                    .defineSubnet("Front-end")
                    .withAddressPrefix("172.16.1.0/24")
                    .attach()
                    .defineSubnet("Back-end")
                    .withAddressPrefix("172.16.2.0/24")
                    .attach()
                    .create();

            System.out.println("Created a virtual network: " + network.id());
            Utils.print(network);

            //============================================================
            // Create a network security group for the front end of a subnet
            // front end subnet contains two rules
            // - ALLOW-SSH - allows SSH traffic into the front end subnet
            // - ALLOW-WEB- allows HTTP traffic into the front end subnet

            System.out.println("Creating a security group for the front end - allows SSH and HTTP");
            NetworkSecurityGroup frontEndNSG = azureResourceManager.networkSecurityGroups().define(frontEndNSGName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .defineRule("ALLOW-SSH")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(22)
                    .withProtocol(SecurityRuleProtocol.TCP)
                    .withPriority(100)
                    .withDescription("Allow SSH")
                    .attach()
                    .defineRule("ALLOW-HTTP")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(80)
                    .withProtocol(SecurityRuleProtocol.TCP)
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
            // - DENY-WEB - denies all outbound internet traffic from the back end subnet

            System.out.println("Creating a security group for the front end - allows SSH and "
                    + "denies all outbound internet traffic  ");

            NetworkSecurityGroup backEndNSG = azureResourceManager.networkSecurityGroups().define(backEndNSGName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .defineRule("ALLOW-SQL")
                    .allowInbound()
                    .fromAddress("172.16.1.0/24")
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(1433)
                    .withProtocol(SecurityRuleProtocol.TCP)
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

            System.out.println("Creating multiple network interfaces");
            System.out.println("Creating network interface 1");


            //========================================================
            // Create a network interface and apply the
            // front end network security group

            System.out.println("Creating a network interface for the front end");

            NetworkInterface networkInterface1 = azureResourceManager.networkInterfaces().define(networkInterfaceName1)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Front-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPAddressLeafDNS1)
                    .withIPForwarding()
                    .withExistingNetworkSecurityGroup(frontEndNSG)
                    .create();

            System.out.println("Created network interface for the front end");

            Utils.print(networkInterface1);


            //========================================================
            // Create a network interface and apply the
            // back end network security group

            System.out.println("Creating a network interface for the back end");

            NetworkInterface networkInterface2 = azureResourceManager.networkInterfaces().define(networkInterfaceName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Back-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingNetworkSecurityGroup(backEndNSG)
                    .create();

            Utils.print(networkInterface2);


            //=============================================================
            // Create a virtual machine (for the front end)
            // with the network interface that has the network security group for the front end

            System.out.println("Creating a Linux virtual machine (for the front end) - "
                    + "with the network interface that has the network security group for the front end");

            Date t1 = new Date();

            VirtualMachine frontEndVM = azureResourceManager.virtualMachines().define(frontEndVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetworkInterface(networkInterface1)
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


            //=============================================================
            // Create a virtual machine (for the back end)
            // with the network interface that has the network security group for the back end

            System.out.println("Creating a Linux virtual machine (for the back end) - "
                    + "with the network interface that has the network security group for the back end");

            t1 = new Date();

            VirtualMachine backEndVM = azureResourceManager.virtualMachines().define(backEndVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetworkInterface(networkInterface2)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            t2 = new Date();
            System.out.println("Created a Linux VM: (took "
                    + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + backEndVM.id());
            Utils.print(backEndVM);


            //========================================================
            // List network security groups

            System.out.println("Walking through network security groups");
            PagedIterable<NetworkSecurityGroup> networkSecurityGroups = azureResourceManager.networkSecurityGroups().listByResourceGroup(rgName);

            for (NetworkSecurityGroup networkSecurityGroup : networkSecurityGroups) {
                Utils.print(networkSecurityGroup);
            }


            //========================================================
            // Update a network security group

            System.out.println("Updating the front end network security group to allow FTP");

            frontEndNSG.update()
                    .defineRule("ALLOW-FTP")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPortRange(20, 21)
                    .withProtocol(SecurityRuleProtocol.TCP)
                    .withDescription("Allow FTP")
                    .withPriority(200)
                    .attach()
                    .apply();

            System.out.println("Updated the front end network security group");
            Utils.print(frontEndNSG);
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

    private ManageNetworkSecurityGroup() {

    }
}
