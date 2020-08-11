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
import com.azure.resourcemanager.network.models.ConnectivityCheck;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkPeering;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Network sample for verifying connectivity between two peered virtual networks using Azure Network Watcher.
 * <p>
 * Summary ...
 * <p>
 * - This sample uses Azure Network Watcher's connectivity check to verify connectivity between
 * two peered virtual networks.
 * <p>
 * Details ...
 * <p>
 * 1. Define two virtual networks network "A" and network "B" with one subnet each
 * <p>
 * 2. Create two virtual machines, each within a separate network
 * - The virtual machines currently must use a special extension to support Network Watcher
 * <p>
 * 3. Peer the networks...
 * - the peering will initially have default settings:
 * - each network's IP address spaces will be accessible from the other network
 * - no traffic forwarding will be enabled between the networks
 * - no gateway transit between one network and the other will be enabled
 * <p>
 * 4. Use Network Watcher to check connectivity between the virtual machines in different peering scenarios:
 * - both virtual machines accessible to each other (bi-directional)
 * - virtual machine A accessible to virtual machine B, but not the other way
 */

public final class VerifyNetworkPeeringWithNetworkWatcher {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_SOUTH_CENTRAL;
        final String resourceGroupName = azure.sdkContext().randomResourceName("rg", 15);
        final String vnetAName = azure.sdkContext().randomResourceName("net", 15);
        final String vnetBName = azure.sdkContext().randomResourceName("net", 15);

        final String[] vmNames = azure.sdkContext().randomResourceNames("vm", 15, 2);
        final String[] vmIPAddresses = new String[]{
                /* within subnetA */ "10.0.0.8",
                /* within subnetB */ "10.1.0.8"
        };

        final String peeringABName = azure.sdkContext().randomResourceName("peer", 15);
        final String rootname = "tirekicker";
        final String password = azure.sdkContext().randomResourceName("pWd!", 15);
        final String networkWatcherName = azure.sdkContext().randomResourceName("netwch", 20);

        try {

            //=============================================================
            // Define two virtual networks to peer and put the virtual machines in, at specific IP addresses
            List<Creatable<Network>> networkDefinitions = new ArrayList<>();

            networkDefinitions.add(azure.networks().define(vnetAName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withAddressSpace("10.0.0.0/27")
                    .withSubnet("subnetA", "10.0.0.0/27"));

            networkDefinitions.add(azure.networks().define(vnetBName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withAddressSpace("10.1.0.0/27")
                    .withSubnet("subnetB", "10.1.0.0/27"));

            //=============================================================
            // Define a couple of Linux VMs and place them in each of the networks

            List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();

            for (int i = 0; i < networkDefinitions.size(); i++) {
                vmDefinitions.add(azure.virtualMachines().define(vmNames[i])
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroupName)
                        .withNewPrimaryNetwork(networkDefinitions.get(i))
                        .withPrimaryPrivateIPAddressStatic(vmIPAddresses[i])
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(rootname)
                        .withRootPassword(password)

                        // Extension currently needed for network watcher support
                        .defineNewExtension("packetCapture")
                        .withPublisher("Microsoft.Azure.NetworkWatcher")
                        .withType("NetworkWatcherAgentLinux")
                        .withVersion("1.4")
                        .attach());
            }

            // Create the VMs in parallel for better performance
            System.out.println("Creating virtual machines and virtual networks...");
            CreatedResources<VirtualMachine> createdVMs = azure.virtualMachines().create(vmDefinitions);
            VirtualMachine vmA = createdVMs.get(vmDefinitions.get(0).key());
            VirtualMachine vmB = createdVMs.get(vmDefinitions.get(1).key());
            System.out.println("Created the virtual machines and networks.");

            //=============================================================
            // Peer the two networks using default settings

            Network networkA = vmA.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
            Network networkB = vmB.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();

            Utils.print(networkA);
            Utils.print(networkB);

            System.out.println(
                    "Peering the networks using default settings...\n"
                            + "- Network access enabled\n"
                            + "- Traffic forwarding disabled\n"
                            + "- Gateway use (transit) by the remote network disabled");

            NetworkPeering peeringAB = networkA.peerings().define(peeringABName)
                    .withRemoteNetwork(networkB)
                    .create();

            Utils.print(networkA);
            Utils.print(networkB);

            //=============================================================
            // Check connectivity between the two VMs/networks using Network Watcher
            NetworkWatcher networkWatcher = azure.networkWatchers().define(networkWatcherName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroupName)
                    .create();

            // Verify bi-directional connectivity between the VMs on port 22 (SSH enabled by default on Linux VMs)
            Executable<ConnectivityCheck> connectivityAtoB = networkWatcher.checkConnectivity()
                    .toDestinationAddress(vmIPAddresses[1])
                    .toDestinationPort(22)
                    .fromSourceVirtualMachine(vmA);
            System.out.println("Connectivity from A to B: " + connectivityAtoB.execute().connectionStatus());

            Executable<ConnectivityCheck> connectivityBtoA = networkWatcher.checkConnectivity()
                    .toDestinationAddress(vmIPAddresses[0])
                    .toDestinationPort(22)
                    .fromSourceVirtualMachine(vmB);
            System.out.println("Connectivity from B to A: " + connectivityBtoA.execute().connectionStatus());

            // Change the peering to allow access between A and B
            System.out.println("Changing the peering to disable access between A and B...");
            peeringAB.update()
                    .withoutAccessFromEitherNetwork()
                    .apply();

            Utils.print(networkA);
            Utils.print(networkB);

            // Verify connectivity no longer possible between A and B
            System.out.println("Peering configuration changed.\nNow, A should be unreachable from B, and B should be unreachable from A...");
            System.out.println("Connectivity from A to B: " + connectivityAtoB.execute().connectionStatus());
            System.out.println("Connectivity from B to A: " + connectivityBtoA.execute().connectionStatus());

            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azure.resourceGroups().beginDeleteByName(resourceGroupName);
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
     * @param args parameters
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

    private VerifyNetworkPeeringWithNetworkWatcher() {

    }
}
