/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of virtual network management.
 */
public class TestNetwork {
    /**
     * Test of plain subnets.
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
            List<Subnet> subnets = nsg.refresh().listAssociatedSubnets();
            Assert.assertTrue(subnets.size() == 1);
            Subnet subnet = subnets.get(0);
            Assert.assertTrue(subnet.name().equalsIgnoreCase("subnetB"));
            Assert.assertTrue(subnet.parent().name().equalsIgnoreCase(newName));

            // Verify NSG
            Assert.assertTrue(subnet.networkSecurityGroupId() != null);
            NetworkSecurityGroup nsg2 = subnet.getNetworkSecurityGroup();
            Assert.assertTrue(nsg2 != null);
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
                        .withExistingNetworkSecurityGroup(nsg)
                        .parent()
                    .defineSubnet("subnetD")
                        .withAddressPrefix("141.25.0.16/29")
                        .withExistingNetworkSecurityGroup(nsg)
                        .attach()
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            return resource;
        }

        @Override
        public void print(Network resource) {
            printNetwork(resource);
        }
    }

	/**
	 * Outputs info about a network
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
            info.append("\n\tAssociated NSG: ");

            NetworkSecurityGroup nsg;
            try {
                nsg = subnet.getNetworkSecurityGroup();
            } catch (Exception e) {
                nsg = null;
            }

            info.append((null == nsg) ? "(None)" : nsg.resourceGroupName() + "/" + nsg.name());

            // Show associated route table
            info.append("\n\tAssociated route table: ");
            RouteTable routeTable;
            try {
                routeTable = subnet.getRouteTable();
            } catch (Exception e) {
                routeTable = null;
            }

            info.append((null == routeTable) ? "(None)" : routeTable.resourceGroupName() + "/" + routeTable.name());
        }

        System.out.println(info.toString());
	}
}
