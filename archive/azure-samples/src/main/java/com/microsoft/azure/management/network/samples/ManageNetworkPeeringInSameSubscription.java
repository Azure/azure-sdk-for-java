/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkPeering;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Arrays;

/**
 * Azure Network sample for enabling and updating network peering between two virtual networks
 *
 * Summary ...
 *
 * - This sample creates two virtual networks in the same subscription and then peers them, modifying various options on the peering.
 *
 * Details ...
 *
 * 1. Create two virtual networks, network "A" and network "B"...
 * - network A with two subnets
 * - network B with one subnet
 * - the networks' address spaces must not overlap
 * - the networks must be in the same region
 *
 * 2. Peer the networks...
 * - the peering will initially have default settings:
 *   - each network's IP address spaces will be accessible from the other network
 *   - no traffic forwarding will be enabled between the networks
 *   - no gateway transit between one network and the other will be enabled
 *
 * 3. Update the peering...
 * - disable IP address space between the networks
 * - enable traffic forwarding from network A to network B
 * 
 * 4. Delete the peering
 * - the removal of the peering takes place on both networks, as long as they are in the same subscription
 
 * Notes: 
 * - Once a peering is created, it cannot be pointed at another remote network later.
 * - The address spaces of the peered networks cannot be changed as long as the networks are peered.
 * - Gateway transit scenarios as well as peering networks in different subscriptions are possible but beyond the scope of this sample.
 * - Network peering in reality results in pairs of peering objects: one pointing from one network to the other,
 *   and the other peering object pointing the other way. For simplicity though, the SDK provides a unified way to
 *   manage the peering as a whole, in a single command flow, without the need to duplicate commands for both sides of the peering,
 *   while enforcing the required restrictions between the two peerings automatically, as this sample shows. But it is also possible
 *   to modify each peering separately, which becomes required when working with networks in different subscriptions.
 */

public final class ManageNetworkPeeringInSameSubscription {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST;
        final String resourceGroupName = SdkContext.randomResourceName("rg", 15);
        final String vnetAName = SdkContext.randomResourceName("net", 15);
        final String vnetBName = SdkContext.randomResourceName("net", 15);
        final String peeringABName = SdkContext.randomResourceName("peer", 15);
        try {

            //=============================================================
            // Define two virtual networks to peer

            System.out.println("Creating two virtual networks in the same region and subscription...");

            Creatable<Network> networkADefinition = azure.networks().define(vnetAName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withAddressSpace("10.0.0.0/27")
                    .withSubnet("subnet1", "10.0.0.0/28")
                    .withSubnet("subnet2", "10.0.0.16/28");

            Creatable<Network> networkBDefinition = azure.networks().define(vnetBName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withAddressSpace("10.1.0.0/27")
                    .withSubnet("subnet3", "10.1.0.0/27");

            // Create the networks in parallel for better performance
            CreatedResources<Network> created = azure.networks().create(Arrays.asList(networkADefinition, networkBDefinition));

            // Print virtual network details
            for (Network network : created.values()) {
                Utils.print((Network) network);
                System.out.println();
            }

            // Retrieve the created networks using their definition keys
            Network networkA = created.get(networkADefinition.key());
            Network networkB = created.get(networkBDefinition.key());

            //=============================================================
            // Peer the two networks using default settings

            System.out.println(
                    "Peering the networks using default settings...\n"
                    + "- Network access enabled\n"
                    + "- Traffic forwarding disabled\n"
                    + "- Gateway use (transit) by the peered network disabled");

            NetworkPeering peeringAB = networkA.peerings().define(peeringABName)
                    .withRemoteNetwork(networkB)
                    .create(); // This implicitly creates a matching peering object on network B as well, if both networks are in the same subscription

            // Print network details showing new peering
            System.out.println("Created a peering");
            Utils.print(networkA);
            Utils.print(networkB);

            //=============================================================
            // Update a the peering disallowing access from B to A but allowing traffic forwarding from B to A

            System.out.println("Updating the peering ...");
            peeringAB.update()
                .withoutAccessFromEitherNetwork()
                .withTrafficForwardingFromRemoteNetwork()
                .apply();

            System.out.println("Updated the peering to disallow network access between B and A but allow traffic forwarding from B to A.");

            //=============================================================
            // Show the new network information

            Utils.print(networkA);
            Utils.print(networkB);

            //=============================================================
            // Remove the peering

            System.out.println("Deleting the peering from the networks...");
            networkA.peerings().deleteById(peeringAB.id()); // This deletes the peering from both networks, if they're in the same subscription
            System.out.println("Deleted the peering from both sides.");

            Utils.print(networkA);
            Utils.print(networkB);

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
     * @param args parameters
     */

    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.BODY.withPrettyJson(true))
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
    private ManageNetworkPeeringInSameSubscription() {

    }
}
