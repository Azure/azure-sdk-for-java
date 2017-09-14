/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import com.microsoft.azure.management.network.NetworkPeeringGatewayUse;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkPeering;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.VirtualNetworkPeeringState;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Test of virtual network management.
 */
public class TestNetwork {
    /**
     * Test of network with subnets.
     */
    public static class WithSubnets extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {
            final String newName = "net" + this.testId;
            Region region = Region.US_WEST;
            String groupName = "rg" + this.testId;

            // Create an NSG
            NetworkSecurityGroup nsg = networks.manager().networkSecurityGroups().define("nsg" + this.testId)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .create();

            // Create a network
            final Network network = networks.define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnetA", "10.0.0.0/29")
                    .defineSubnet("subnetB")
                        .withAddressPrefix("10.0.0.8/29")
                        .withExistingNetworkSecurityGroup(nsg)
                        .attach()
                    .create();

            // Verify subnets
            Assert.assertEquals(2, network.subnets().size());
            Subnet subnet = network.subnets().get("subnetA");
            Assert.assertEquals("10.0.0.0/29", subnet.addressPrefix());

            subnet = network.subnets().get("subnetB");
            Assert.assertEquals("10.0.0.8/29", subnet.addressPrefix());
            Assert.assertTrue(nsg.id().equalsIgnoreCase(subnet.networkSecurityGroupId()));

            // Verify NSG
            List<Subnet> subnets = nsg.refresh().listAssociatedSubnets();
            Assert.assertEquals(1, subnets.size());
            subnet = subnets.get(0);
            Assert.assertTrue(subnet.name().equalsIgnoreCase("subnetB"));
            Assert.assertTrue(subnet.parent().name().equalsIgnoreCase(newName));
            Assert.assertNotNull(subnet.networkSecurityGroupId());
            NetworkSecurityGroup nsg2 = subnet.getNetworkSecurityGroup();
            Assert.assertNotNull(nsg2);
            Assert.assertTrue(nsg2.id().equalsIgnoreCase(nsg.id()));

            return network;
        }

        @Override
        public Network updateResource(Network resource) throws Exception {
            NetworkSecurityGroup nsg = resource.manager().networkSecurityGroups().define("nsgB" + this.testId)
                    .withRegion(resource.region())
                    .withExistingResourceGroup(resource.resourceGroupName())
                    .create();

            resource =  resource.update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .withAddressSpace("141.25.0.0/16")
                    .withSubnet("subnetC", "141.25.0.0/29")
                    .withoutSubnet("subnetA")
                    .updateSubnet("subnetB")
                        .withAddressPrefix("141.25.0.8/29")
                        .withoutNetworkSecurityGroup()
                        .parent()
                    .defineSubnet("subnetD")
                        .withAddressPrefix("141.25.0.16/29")
                        .withExistingNetworkSecurityGroup(nsg)
                        .attach()
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify subnets
            Assert.assertEquals(3, resource.subnets().size());
            Assert.assertFalse(resource.subnets().containsKey("subnetA"));

            Subnet subnet = resource.subnets().get("subnetB");
            Assert.assertNotNull(subnet);
            Assert.assertEquals("141.25.0.8/29", subnet.addressPrefix());
            Assert.assertNull(subnet.networkSecurityGroupId());

            subnet = resource.subnets().get("subnetC");
            Assert.assertNotNull(subnet);
            Assert.assertEquals("141.25.0.0/29", subnet.addressPrefix());
            Assert.assertNull(subnet.networkSecurityGroupId());

            subnet = resource.subnets().get("subnetD");
            Assert.assertNotNull(subnet);
            Assert.assertEquals("141.25.0.16/29", subnet.addressPrefix());
            Assert.assertTrue(nsg.id().equalsIgnoreCase(subnet.networkSecurityGroupId()));

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /**
     * Test of network peerings.
     */
    public static class WithPeering extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {
            Region region = Region.US_EAST;
            String groupName = "rg" + this.testId;

            String networkName = SdkContext.randomResourceName("net", 15);
            String networkName2 = SdkContext.randomResourceName("net", 15);

            Creatable<Network> remoteNetworkDefinition = networks.define(networkName2)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.1.0.0/27")
                    .withSubnet("subnet3", "10.1.0.0/27");

            Creatable<Network> localNetworkDefinition = networks.define(networkName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/27")
                    .withSubnet("subnet1", "10.0.0.0/28")
                    .withSubnet("subnet2", "10.0.0.16/28");

            CreatedResources<Network> createdNetworks = networks.create(Arrays.asList(remoteNetworkDefinition, localNetworkDefinition));
            Network localNetwork = createdNetworks.get(localNetworkDefinition.key());
            Network remoteNetwork = createdNetworks.get(remoteNetworkDefinition.key());
            Assert.assertNotNull(localNetwork);
            Assert.assertNotNull(remoteNetwork);

            // Create peering
            NetworkPeering localPeering = localNetwork.peerings().define("peer0")
                .withRemoteNetwork(remoteNetwork)

                // Optionals
                .withTrafficForwardingBetweenBothNetworks()
                .withoutAccessFromEitherNetwork()
                .withGatewayUseByRemoteNetworkAllowed()
                .create();

            // Verify local peering
            Assert.assertNotNull(localNetwork.peerings());
            Assert.assertEquals(1,  localNetwork.peerings().list().size());
            localPeering = localNetwork.peerings().list().get(0);
            Assert.assertNotNull(localPeering);
            Assert.assertTrue(localPeering.name().equalsIgnoreCase("peer0"));
            Assert.assertEquals(VirtualNetworkPeeringState.CONNECTED, localPeering.state());
            Assert.assertTrue(localPeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assert.assertFalse(localPeering.checkAccessBetweenNetworks());
            Assert.assertEquals(NetworkPeeringGatewayUse.BY_REMOTE_NETWORK, localPeering.gatewayUse());

            // Verify remote peering
            Assert.assertNotNull(remoteNetwork.peerings());
            Assert.assertEquals(1, remoteNetwork.peerings().list().size());
            NetworkPeering remotePeering = localPeering.getRemotePeering();
            Assert.assertNotNull(remotePeering);
            Assert.assertTrue(remotePeering.remoteNetworkId().equalsIgnoreCase(localNetwork.id()));
            Assert.assertEquals(VirtualNetworkPeeringState.CONNECTED, remotePeering.state());
            Assert.assertTrue(remotePeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assert.assertFalse(remotePeering.checkAccessBetweenNetworks());
            Assert.assertEquals(NetworkPeeringGatewayUse.NONE, remotePeering.gatewayUse());

            return localNetwork;
        }

        @Override
        public Network updateResource(Network resource) throws Exception {
            NetworkPeering localPeering = resource.peerings().list().get(0);

            // Verify remote IP invisibility to local network before peering
            Network remoteNetwork = localPeering.getRemoteNetwork();
            Assert.assertNotNull(remoteNetwork);
            Subnet remoteSubnet = remoteNetwork.subnets().get("subnet3");
            Assert.assertNotNull(remoteSubnet);
            Set<String> remoteAvailableIPs = remoteSubnet.listAvailablePrivateIPAddresses();
            Assert.assertNotNull(remoteAvailableIPs);
            Assert.assertFalse(remoteAvailableIPs.isEmpty());
            String remoteTestIP = remoteAvailableIPs.iterator().next();
            Assert.assertFalse(resource.isPrivateIPAddressAvailable(remoteTestIP));

            localPeering.update()
                .withoutTrafficForwardingFromEitherNetwork()
                .withAccessBetweenBothNetworks()
                .withoutAnyGatewayUse()
                .apply();

            // Verify local peering changes
            Assert.assertFalse(localPeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assert.assertTrue(localPeering.checkAccessBetweenNetworks());
            Assert.assertEquals(NetworkPeeringGatewayUse.NONE, localPeering.gatewayUse());

            // Verify remote peering changes
            NetworkPeering remotePeering = localPeering.getRemotePeering();
            Assert.assertNotNull(remotePeering);
            Assert.assertFalse(remotePeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assert.assertTrue(remotePeering.checkAccessBetweenNetworks());
            Assert.assertEquals(NetworkPeeringGatewayUse.NONE, remotePeering.gatewayUse());

            // Delete the peering
            resource.peerings().deleteById(remotePeering.id());

            // Verify deletion
            Assert.assertEquals(0, resource.peerings().list().size());
            Assert.assertEquals(0, remoteNetwork.peerings().list().size());

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /**
     * Outputs info about a network.
     * @param resource a network
     */
    public static void printNetwork(Network resource) {
        StringBuilder info = new StringBuilder();
        info.append("Network: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tAddress spaces: ").append(resource.addressSpaces())
                .append("\n\tDNS server IPs: ").append(resource.dnsServerIPs());

        // Output subnets
        for (Subnet subnet : resource.subnets().values()) {
            info.append("\n\tSubnet: ").append(subnet.name())
                .append("\n\t\tAddress prefix: ").append(subnet.addressPrefix());

            // Show associated NSG
            NetworkSecurityGroup nsg = subnet.getNetworkSecurityGroup();
            if (nsg != null) {
                info.append("\n\tNetwork security group ID: ").append(nsg.id());
            }

            // Show associated route table
            RouteTable routeTable = subnet.getRouteTable();
            if (routeTable != null) {
                info.append("\n\tRoute table ID: ").append(routeTable.id());
            }
        }

        // Output peerings
        for (NetworkPeering peering : resource.peerings().list()) {
            info.append("\n\tPeering: ").append(peering.name())
                .append("\n\t\tRemote network ID: ").append(peering.remoteNetworkId())
                .append("\n\t\tPeering state: ").append(peering.state())
                .append("\n\t\tIs traffic forwarded from remote network allowed? ").append(peering.isTrafficForwardingFromRemoteNetworkAllowed())
                //TODO .append("\n\t\tIs access from remote network allowed? ").append(peering.isAccessBetweenNetworksAllowed())
                .append("\n\t\tGateway use: ").append(peering.gatewayUse());
        }

        System.out.println(info.toString());
    }
}
