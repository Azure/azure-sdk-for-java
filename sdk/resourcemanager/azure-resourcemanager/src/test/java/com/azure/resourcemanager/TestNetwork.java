// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkPeering;
import com.azure.resourcemanager.network.models.NetworkPeeringGatewayUse;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.Networks;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.VirtualNetworkPeeringState;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;

/** Test of virtual network management. */
public class TestNetwork {
    /** Test of network with subnets. */
    public class WithSubnets extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {

            String postFix = networks.manager().sdkContext().randomResourceName("", 8);
            final String newName = "net" + postFix;
            Region region = Region.US_WEST;
            String groupName = "rg" + postFix;

            // Create an NSG
            NetworkSecurityGroup nsg =
                networks
                    .manager()
                    .networkSecurityGroups()
                    .define("nsg" + postFix)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .create();

            // Create a network
            final Network network =
                networks
                    .define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/28")
                    .withAddressSpace("10.1.0.0/28")
                    .withSubnet("subnetA", "10.0.0.0/29")
                    .defineSubnet("subnetB")
                    .withAddressPrefix("10.0.0.8/29")
                    .withExistingNetworkSecurityGroup(nsg)
                    .attach()
                    .create();

            // Verify address spaces
            Assertions.assertEquals(2, network.addressSpaces().size());
            Assertions.assertTrue(network.addressSpaces().contains("10.1.0.0/28"));

            // Verify subnets
            Assertions.assertEquals(2, network.subnets().size());
            Subnet subnet = network.subnets().get("subnetA");
            Assertions.assertEquals("10.0.0.0/29", subnet.addressPrefix());

            subnet = network.subnets().get("subnetB");
            Assertions.assertEquals("10.0.0.8/29", subnet.addressPrefix());
            Assertions.assertTrue(nsg.id().equalsIgnoreCase(subnet.networkSecurityGroupId()));

            // Verify NSG
            List<Subnet> subnets = nsg.refresh().listAssociatedSubnets();
            Assertions.assertEquals(1, subnets.size());
            subnet = subnets.get(0);
            Assertions.assertTrue(subnet.name().equalsIgnoreCase("subnetB"));
            Assertions.assertTrue(subnet.parent().name().equalsIgnoreCase(newName));
            Assertions.assertNotNull(subnet.networkSecurityGroupId());
            NetworkSecurityGroup nsg2 = subnet.getNetworkSecurityGroup();
            Assertions.assertNotNull(nsg2);
            Assertions.assertTrue(nsg2.id().equalsIgnoreCase(nsg.id()));

            return network;
        }

        @Override
        public Network updateResource(Network resource) throws Exception {
            NetworkSecurityGroup nsg =
                resource
                    .manager()
                    .networkSecurityGroups()
                    .define(resource.manager().sdkContext().randomResourceName("nsgB", 10))
                    .withRegion(resource.region())
                    .withExistingResourceGroup(resource.resourceGroupName())
                    .create();

            resource =
                resource
                    .update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .withAddressSpace("141.25.0.0/16")
                    .withoutAddressSpace("10.1.0.0/28")
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
            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify address spaces
            Assertions.assertEquals(2, resource.addressSpaces().size());
            Assertions.assertFalse(resource.addressSpaces().contains("10.1.0.0/28"));

            // Verify subnets
            Assertions.assertEquals(3, resource.subnets().size());
            Assertions.assertFalse(resource.subnets().containsKey("subnetA"));

            Subnet subnet = resource.subnets().get("subnetB");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.8/29", subnet.addressPrefix());
            Assertions.assertNull(subnet.networkSecurityGroupId());

            subnet = resource.subnets().get("subnetC");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.0/29", subnet.addressPrefix());
            Assertions.assertNull(subnet.networkSecurityGroupId());

            subnet = resource.subnets().get("subnetD");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.16/29", subnet.addressPrefix());
            Assertions.assertTrue(nsg.id().equalsIgnoreCase(subnet.networkSecurityGroupId()));

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /** Test of network with subnets configured to have access from azure service. */
    public class WithAccessFromServiceToSubnet extends TestTemplate<Network, Networks> {

        @Override
        public Network createResource(Networks networks) throws Exception {
            String postfix = networks.manager().sdkContext().randomResourceName("", 8);
            final String newName = "net" + postfix;
            Region region = Region.US_WEST;
            String groupName = "rg" + postfix;

            // Create a network
            final Network network =
                networks
                    .define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnetA", "10.0.0.0/29")
                    .defineSubnet("subnetB")
                    .withAddressPrefix("10.0.0.8/29")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_STORAGE)
                    .attach()
                    .create();

            // Verify address spaces
            Assertions.assertEquals(1, network.addressSpaces().size());
            Assertions.assertTrue(network.addressSpaces().contains("10.0.0.0/28"));

            // Verify subnets
            Assertions.assertEquals(2, network.subnets().size());
            Subnet subnet = network.subnets().get("subnetA");
            Assertions.assertEquals("10.0.0.0/29", subnet.addressPrefix());

            subnet = network.subnets().get("subnetB");
            Assertions.assertEquals("10.0.0.8/29", subnet.addressPrefix());
            Assertions.assertNotNull(subnet.servicesWithAccess());
            Assertions.assertTrue(subnet.servicesWithAccess().containsKey(ServiceEndpointType.MICROSOFT_STORAGE));
            Assertions.assertTrue(subnet.servicesWithAccess().get(ServiceEndpointType.MICROSOFT_STORAGE).size() > 0);
            return network;
        }

        @Override
        public Network updateResource(Network resource) throws Exception {
            resource =
                resource
                    .update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .withAddressSpace("141.25.0.0/16")
                    .withoutAddressSpace("10.1.0.0/28")
                    .withSubnet("subnetC", "141.25.0.0/29")
                    .withoutSubnet("subnetA")
                    .updateSubnet("subnetB")
                    .withAddressPrefix("141.25.0.8/29")
                    .withoutAccessFromService(ServiceEndpointType.MICROSOFT_STORAGE)
                    .parent()
                    .defineSubnet("subnetD")
                    .withAddressPrefix("141.25.0.16/29")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_STORAGE)
                    .attach()
                    .apply();

            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify address spaces
            Assertions.assertEquals(2, resource.addressSpaces().size());
            Assertions.assertFalse(resource.addressSpaces().contains("10.1.0.0/28"));

            // Verify subnets
            Assertions.assertEquals(3, resource.subnets().size());
            Assertions.assertFalse(resource.subnets().containsKey("subnetA"));

            Subnet subnet = resource.subnets().get("subnetB");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.8/29", subnet.addressPrefix());
            Assertions.assertNotNull(subnet.servicesWithAccess());
            Assertions.assertTrue(subnet.servicesWithAccess().isEmpty());

            subnet = resource.subnets().get("subnetC");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.0/29", subnet.addressPrefix());

            subnet = resource.subnets().get("subnetD");
            Assertions.assertNotNull(subnet);
            Assertions.assertEquals("141.25.0.16/29", subnet.addressPrefix());
            Assertions.assertNotNull(subnet.servicesWithAccess());
            Assertions.assertTrue(subnet.servicesWithAccess().containsKey(ServiceEndpointType.MICROSOFT_STORAGE));

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /** Test of network peerings. */
    public class WithPeering extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {
            Region region = Region.US_EAST;
            String groupName = networks.manager().sdkContext().randomResourceName("rg", 10);

            String networkName = networks.manager().sdkContext().randomResourceName("net", 15);
            String networkName2 = networks.manager().sdkContext().randomResourceName("net", 15);

            Creatable<Network> remoteNetworkDefinition =
                networks
                    .define(networkName2)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.1.0.0/27")
                    .withSubnet("subnet3", "10.1.0.0/27");

            Creatable<Network> localNetworkDefinition =
                networks
                    .define(networkName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/27")
                    .withSubnet("subnet1", "10.0.0.0/28")
                    .withSubnet("subnet2", "10.0.0.16/28");

            CreatedResources<Network> createdNetworks =
                networks.create(Arrays.asList(remoteNetworkDefinition, localNetworkDefinition));
            Network localNetwork = createdNetworks.get(localNetworkDefinition.key());
            Network remoteNetwork = createdNetworks.get(remoteNetworkDefinition.key());
            Assertions.assertNotNull(localNetwork);
            Assertions.assertNotNull(remoteNetwork);

            // Create peering
            NetworkPeering localPeering =
                localNetwork
                    .peerings()
                    .define("peer0")
                    .withRemoteNetwork(remoteNetwork)

                    // Optionals
                    .withTrafficForwardingBetweenBothNetworks()
                    .withoutAccessFromEitherNetwork()
                    .withGatewayUseByRemoteNetworkAllowed()
                    .create();

            // Verify local peering
            Assertions.assertNotNull(localNetwork.peerings());
            Assertions.assertEquals(1, TestUtilities.getSize(localNetwork.peerings().list()));
            Assertions.assertEquals(1, localPeering.remoteAddressSpaces().size());
            Assertions.assertEquals("10.1.0.0/27", localPeering.remoteAddressSpaces().get(0));
            localPeering = localNetwork.peerings().list().iterator().next();
            Assertions.assertNotNull(localPeering);
            Assertions.assertTrue(localPeering.name().equalsIgnoreCase("peer0"));
            Assertions.assertEquals(VirtualNetworkPeeringState.CONNECTED, localPeering.state());
            Assertions.assertTrue(localPeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assertions.assertFalse(localPeering.checkAccessBetweenNetworks());
            Assertions.assertEquals(NetworkPeeringGatewayUse.BY_REMOTE_NETWORK, localPeering.gatewayUse());

            // Verify remote peering
            Assertions.assertNotNull(remoteNetwork.peerings());
            Assertions.assertEquals(1, TestUtilities.getSize(remoteNetwork.peerings().list()));
            NetworkPeering remotePeering = localPeering.getRemotePeering();
            Assertions.assertNotNull(remotePeering);
            Assertions.assertTrue(remotePeering.remoteNetworkId().equalsIgnoreCase(localNetwork.id()));
            Assertions.assertEquals(VirtualNetworkPeeringState.CONNECTED, remotePeering.state());
            Assertions.assertTrue(remotePeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assertions.assertFalse(remotePeering.checkAccessBetweenNetworks());
            Assertions.assertEquals(NetworkPeeringGatewayUse.NONE, remotePeering.gatewayUse());

            return localNetwork;
        }

        @Override
        public Network updateResource(Network resource) throws Exception {
            NetworkPeering localPeering = resource.peerings().list().iterator().next();

            // Verify remote IP invisibility to local network before peering
            Network remoteNetwork = localPeering.getRemoteNetwork();
            Assertions.assertNotNull(remoteNetwork);
            Subnet remoteSubnet = remoteNetwork.subnets().get("subnet3");
            Assertions.assertNotNull(remoteSubnet);
            Set<String> remoteAvailableIPs = remoteSubnet.listAvailablePrivateIPAddresses();
            Assertions.assertNotNull(remoteAvailableIPs);
            Assertions.assertFalse(remoteAvailableIPs.isEmpty());
            String remoteTestIP = remoteAvailableIPs.iterator().next();
            Assertions.assertFalse(resource.isPrivateIPAddressAvailable(remoteTestIP));

            localPeering
                .update()
                .withoutTrafficForwardingFromEitherNetwork()
                .withAccessBetweenBothNetworks()
                .withoutAnyGatewayUse()
                .apply();

            // Verify local peering changes
            Assertions.assertFalse(localPeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assertions.assertTrue(localPeering.checkAccessBetweenNetworks());
            Assertions.assertEquals(NetworkPeeringGatewayUse.NONE, localPeering.gatewayUse());

            // Verify remote peering changes
            NetworkPeering remotePeering = localPeering.getRemotePeering();
            Assertions.assertNotNull(remotePeering);
            Assertions.assertFalse(remotePeering.isTrafficForwardingFromRemoteNetworkAllowed());
            Assertions.assertTrue(remotePeering.checkAccessBetweenNetworks());
            Assertions.assertEquals(NetworkPeeringGatewayUse.NONE, remotePeering.gatewayUse());

            // Delete the peering
            resource.peerings().deleteById(remotePeering.id());

            // Verify deletion
            Assertions.assertEquals(0, TestUtilities.getSize(resource.peerings().list()));
            Assertions.assertEquals(0, TestUtilities.getSize(remoteNetwork.peerings().list()));

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /** Test of network with DDoS protection plan. */
    public class WithDDosProtectionPlanAndVmProtection extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {
            Region region = Region.US_EAST2;
            String groupName = networks.manager().sdkContext().randomResourceName("rg", 10);

            String networkName = networks.manager().sdkContext().randomResourceName("net", 15);

            Network network =
                networks
                    .define(networkName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withNewDdosProtectionPlan()
                    .withVmProtection()
                    .create();
            Assertions.assertTrue(network.isDdosProtectionEnabled());
            Assertions.assertNotNull(network.ddosProtectionPlanId());
            Assertions.assertTrue(network.isVmProtectionEnabled());

            return network;
        }

        @Override
        public Network updateResource(Network network) throws Exception {
            network.update().withoutDdosProtectionPlan().withoutVmProtection().apply();
            Assertions.assertFalse(network.isDdosProtectionEnabled());
            Assertions.assertNull(network.ddosProtectionPlanId());
            Assertions.assertFalse(network.isVmProtectionEnabled());
            return network;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /** Test of network updateTags functionality. */
    public class WithUpdateTags extends TestTemplate<Network, Networks> {
        @Override
        public Network createResource(Networks networks) throws Exception {
            Region region = Region.US_SOUTH_CENTRAL;
            String groupName = networks.manager().sdkContext().randomResourceName("rg", 10);

            String networkName = networks.manager().sdkContext().randomResourceName("net", 15);

            Network network =
                networks
                    .define(networkName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withTag("tag1", "value1")
                    .create();
            Assertions.assertEquals("value1", network.tags().get("tag1"));
            return network;
        }

        @Override
        public Network updateResource(Network network) throws Exception {
            network.updateTags().withoutTag("tag1").withTag("tag2", "value2").applyTags();
            Assertions.assertFalse(network.tags().containsKey("tag1"));
            Assertions.assertEquals("value2", network.tags().get("tag2"));
            return network;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

    /**
     * Outputs info about a network.
     *
     * @param resource a network
     */
    public static void printNetwork(Network resource) {
        StringBuilder info = new StringBuilder();
        info
            .append("Network: ")
            .append(resource.id())
            .append("Name: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags())
            .append("\n\tAddress spaces: ")
            .append(resource.addressSpaces())
            .append("\n\tDNS server IPs: ")
            .append(resource.dnsServerIPs());

        // Output subnets
        for (Subnet subnet : resource.subnets().values()) {
            info
                .append("\n\tSubnet: ")
                .append(subnet.name())
                .append("\n\t\tAddress prefix: ")
                .append(subnet.addressPrefix());

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

            // Output services with access
            Map<ServiceEndpointType, List<Region>> services = subnet.servicesWithAccess();
            if (services.size() > 0) {
                info.append("\n\tServices with access");
                for (Map.Entry<ServiceEndpointType, List<Region>> service : services.entrySet()) {
                    info
                        .append("\n\t\tService: ")
                        .append(service.getKey())
                        .append(" Regions: " + service.getValue() + "");
                }
            }
        }

        // Output peerings
        for (NetworkPeering peering : resource.peerings().list()) {
            info
                .append("\n\tPeering: ")
                .append(peering.name())
                .append("\n\t\tRemote network ID: ")
                .append(peering.remoteNetworkId())
                .append("\n\t\tPeering state: ")
                .append(peering.state())
                .append("\n\t\tIs traffic forwarded from remote network allowed? ")
                .append(peering.isTrafficForwardingFromRemoteNetworkAllowed())
                // TODO (weidxu) .append("\n\t\tIs access from remote network allowed?
                // ").append(peering.isAccessBetweenNetworksAllowed())
                .append("\n\t\tGateway use: ")
                .append(peering.gatewayUse());
        }

        System.out.println(info.toString());
    }
}
