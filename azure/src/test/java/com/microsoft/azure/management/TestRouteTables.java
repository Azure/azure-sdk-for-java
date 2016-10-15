/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.microsoft.azure.management.network.Route;
import com.microsoft.azure.management.network.RouteNextHopType;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.network.RouteTables;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of virtual network management.
 */
public class TestRouteTables {
    /**
     * Test of minimal route tables.
     */
    public static class Minimal extends TestTemplate<RouteTable, RouteTables> {
        @Override
        public RouteTable createResource(RouteTables routeTables) throws Exception {
            final String newName = "rt" + this.testId;
            Region region = Region.US_WEST;
            String groupName = "rg" + this.testId;

            // Create a route table
            final RouteTable routeTable = routeTables.define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .defineRoute("route1")
                        .withDestinationAddressPrefix("10.1.0.0/29")
                        .withNextHopToVirtualAppliance("10.1.1.1")
                        .attach()
                    .defineRoute("route2")
                        .withDestinationAddressPrefix("10.0.0.0/29")
                        .withNextHop(RouteNextHopType.VNET_LOCAL)
                        .attach()
                    .create();

            return routeTable;
        }

        @Override
        public RouteTable updateResource(RouteTable routeTable) throws Exception {
            routeTable =  routeTable.update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(routeTable.tags().containsKey("tag1"));
            return routeTable;
        }

        @Override
        public void print(RouteTable resource) {
            printRouteTable(resource);
        }
    }

	/**
	 * Outputs info about a route table
	 * @param resource a route table
	 */
	public static void printRouteTable(RouteTable resource) {
        StringBuilder info = new StringBuilder();
        info.append("Route table: ").append(resource.id())
                .append("\n\tName: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

        // Output routes
        Map<String, Route> routes = resource.routes();
        info.append("\n\tRoutes: ").append(routes.values().size());
        for (Route route : routes.values()) {
            info.append("\n\t\tName: ").append(route.name())
                .append("\n\t\t\tDestination address prefix: ").append(route.destinationAddressPrefix())
                .append("\n\t\t\tNext hop type: ").append(route.nextHopType().toString())
                .append("\n\t\t\tNext hop IP address: ").append(route.nextHopIpAddress());
        }

        // Output associated subnets
        List<Subnet> subnets = resource.listAssociatedSubnets();
        info.append("\n\tAssociated subnets: ").append(subnets.size());
        for (Subnet subnet : subnets) {
            info.append("\n\t\tResource group: ").append(subnet.parent().resourceGroupName())
                .append("\n\t\tNetwork name: ").append(subnet.parent().name())
                .append("\n\t\tSubnet name: ").append(subnet.name());
        }

        System.out.println(info.toString());
	}
}
