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
import com.microsoft.azure.management.network.Direction;
import com.microsoft.azure.management.network.FlowLogSettings;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.NextHop;
import com.microsoft.azure.management.network.PacketCapture;
import com.microsoft.azure.management.network.PcProtocol;
import com.microsoft.azure.management.network.Protocol;
import com.microsoft.azure.management.network.SecurityGroupView;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.network.VerificationIPFlow;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Arrays;

/**
 * Azure Network sample for managing network watcher -
 *  - Create Network Watcher
 *	- Manage packet capture – track traffic to and from a virtual machine
 *   	Create a VM
 *      Start a packet capture
 *      Stop a packet capture
 *      Get a packet capture
 *      Delete a packet capture
 *      Download a packet capture
 *  - Verify IP flow – verify if traffic is allowed to or from a virtual machine
 *      Get the IP address of a NIC on a virtual machine
 *      Test IP flow on the NIC
 *  - Analyze next hop – get the next hop type and IP address for a virtual machine
 *  - Retrieve network topology for a resource group
 *  - Analyze Virtual Machine Security by examining effective network security rules applied to a VM
 *      Get security group view for the VM
 *  - Configure Network Security Group Flow Logs
 *      Get flow log settings
 *      Enable NSG flow log
 *      Disable NSG flow log
 *      Download a flow log
 *  - Delete network watcher
 */

public final class ManageNetworkWatcher {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_NORTH_CENTRAL;
        final String nwName = SdkContext.randomResourceName("nw", 8);

        final String userName = "tirekicker";
        final String vnetName = SdkContext.randomResourceName("vnet", 20);
        final String dnsLabel = SdkContext.randomResourceName("pipdns", 20);
        final String subnetName = "subnet1";
        final String nsgName = SdkContext.randomResourceName("nsg", 20);
        final String rgName = SdkContext.randomResourceName("rg", 24);
        final String saName = SdkContext.randomResourceName("sa", 24);
        final String vmName = SdkContext.randomResourceName("vm", 24);
        final String packetCaptureName = SdkContext.randomResourceName("pc", 8);

        NetworkWatcher nw = null;
        try {
            //============================================================
            // Create network watcher
            System.out.println("Creating network watcher...");
            nw = azure.networkWatchers().define(nwName)
                    .withRegion(region)
                    .withNewResourceGroup()
                    .create();
            System.out.println("Created network watcher");
            // Print the network watcher
            Utils.print(nw);

            //============================================================
            // Manage packet capture – track traffic to and from a virtual machine

            // Create network security group, virtual network and VM; add packetCapture extension to enable
            System.out.println("Creating network security group...");
            NetworkSecurityGroup nsg = azure.networkSecurityGroups().define(nsgName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .defineRule("DenyInternetInComing")
                        .denyInbound()
                        .fromAddress("INTERNET")
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(443)
                        .withAnyProtocol()
                        .attach()
                    .create();
            System.out.println("Creating virtual network...");
            Network virtualNetwork = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace("192.168.0.0/16")
                    .defineSubnet(subnetName)
                        .withAddressPrefix("192.168.2.0/24")
                        .withExistingNetworkSecurityGroup(nsg)
                        .attach()
                    .create();
            System.out.println("Creating virtual machine...");
            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(virtualNetwork)
                    .withSubnet(subnetName)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(dnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456")
                    .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                    .defineNewExtension("packetCapture")
                        .withPublisher("Microsoft.Azure.NetworkWatcher")
                        .withType("NetworkWatcherAgentLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .attach()
                    .create();

            // Create storage account
            System.out.println("Creating storage account...");
            StorageAccount storageAccount = azure.storageAccounts().define(saName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .create();

            // Start a packet capture
            System.out.println("Creating packet capture...");
            PacketCapture packetCapture = nw.packetCaptures()
                    .define(packetCaptureName)
                    .withTarget(vm.id())
                    .withStorageAccountId(storageAccount.id())
                    .withTimeLimitInSeconds(1500)
                    .definePacketCaptureFilter()
                        .withProtocol(PcProtocol.TCP)
                        .withLocalIPAddresses(Arrays.asList("127.0.0.1", "127.0.0.5"))
                        .attach()
                    .create();
            System.out.println("Created packet capture");
            Utils.print(packetCapture);

            // Stop a packet capture
            System.out.println("Stopping packet capture...");
            packetCapture.stop();
            Utils.print(packetCapture);

            // Get a packet capture
            System.out.printf("Getting packet capture...");
            PacketCapture packetCapture1 = nw.packetCaptures().getByName(packetCaptureName);
            Utils.print(packetCapture1);

            // Delete a packet capture
            System.out.println("Deleting packet capture");
            nw.packetCaptures().deleteByName(packetCapture.name());

            // Download a packet capture


            //============================================================
            // Verify IP flow – verify if traffic is allowed to or from a virtual machine
            // Get the IP address of a NIC on a virtual machine
            String ipAddress = vm.getPrimaryNetworkInterface().ipConfigurations().values().iterator().next().privateIPAddress();
            // Test IP flow on the NIC
            System.out.println("Verifying IP flow for vm id " + vm.id() + "...");
            VerificationIPFlow verificationIPFlow = nw.verifyIPFlow()
                    .withTargetResourceId(vm.id())
                    .withDirection(Direction.INBOUND)
                    .withProtocol(Protocol.TCP)
                    .withLocalIPAddress(ipAddress)
                    .withRemoteIPAddress("8.8.8.8")
                    .withLocalPort("443")
                    .withRemotePort("443")
                    .execute();
            Utils.print(verificationIPFlow);

            //============================================================
            // Analyze next hop – get the next hop type and IP address for a virtual machine
            System.out.println("Calculating next hop...");
            NextHop nextHop = nw.nextHop().withTargetResourceId(vm.id())
                    .withSourceIPAddress(ipAddress)
                    .withDestinationIPAddress("8.8.8.8")
                    .execute();
            Utils.print(nextHop);

            //============================================================
            // Retrieve network topology for a resource group
            System.out.println("Getting topology...");
            Topology topology = nw.getTopology(rgName);
            Utils.print(topology);

            //============================================================
            // Analyze Virtual Machine Security by examining effective network security rules applied to a VM
            // Get security group view for the VM
            System.out.println("Getting security group view for a vm");
            SecurityGroupView sgViewResult = nw.getSecurityGroupView(vm.id());
            Utils.print(sgViewResult);

            //============================================================
            // Configure Network Security Group Flow Logs

            // Get flow log settings
            FlowLogSettings flowLogSettings = nw.getFlowLogSettings(nsg.id());
            Utils.print(flowLogSettings);

            // Enable NSG flow log
            flowLogSettings.update()
                    .withLogging()
                    .withStorageAccount(storageAccount.id())
                    .withRetentionPolicyDays(5)
                    .withRetentionPolicyEnabled()
                    .apply();
            Utils.print(flowLogSettings);

            // Disable NSG flow log
            flowLogSettings.update()
                    .withoutLogging()
                    .apply();
            Utils.print(flowLogSettings);

            // Download a flow log

            //============================================================
            // Delete network watcher
            System.out.println("Deleting network watcher");
            azure.networkWatchers().deleteById(nw.id());
            System.out.println("Deleted network watcher");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (nw != null) {
                    System.out.println("Deleting network watcher: " + nw.name());
                    azure.networkWatchers().deleteById(nw.id());
                }
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

    private ManageNetworkWatcher() {
    }
}