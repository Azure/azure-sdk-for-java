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
    private static String ROUTE1_NAME = "route1";
    private static String ROUTE2_NAME = "route2";
    private static String ROUTE_ADDED_NAME = "route3";
    private static String VIRTUAL_APPLIANCE_IP = "10.1.1.1";

    /**
     * Test of minimal route tables.
     */
    public static class Minimal extends TestTemplate<RouteTable, RouteTables> {
        @Override
        public RouteTable createResource(RouteTables routeTables) throws Exception {
            final String newName = "rt" + this.testId;
            Region region = Region.US_WEST;
            String groupName = "rg" + this.testId;

            final String route1AddressPrefix = "10.0.1.0/29";
            final String route2AddressPrefix = "10.0.0.0/29";
            final RouteNextHopType hopType = RouteNextHopType.VNET_LOCAL;

            // Create a route table
            final RouteTable routeTable = routeTables.define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withRoute("10.0.3.0/29", RouteNextHopType.VNET_LOCAL)
                    .defineRoute(ROUTE1_NAME)
                        .withDestinationAddressPrefix(route1AddressPrefix)
                        .withNextHopToVirtualAppliance(VIRTUAL_APPLIANCE_IP)
                        .attach()
                    .defineRoute(ROUTE2_NAME)
                        .withDestinationAddressPrefix(route2AddressPrefix)
                        .withNextHop(hopType)
                        .attach()
                    .create();

            Assert.assertTrue(routeTable.routes().containsKey(ROUTE1_NAME));
            Route route1 = routeTable.routes().get(ROUTE1_NAME);
            Assert.assertTrue(route1.destinationAddressPrefix().equalsIgnoreCase(route1AddressPrefix));
            Assert.assertTrue(route1.nextHopIPAddress().equalsIgnoreCase(VIRTUAL_APPLIANCE_IP));
            Assert.assertTrue(route1.nextHopType().equals(RouteNextHopType.VIRTUAL_APPLIANCE));

            Assert.assertTrue(routeTable.routes().containsKey(ROUTE2_NAME));
            Route route2 = routeTable.routes().get(ROUTE2_NAME);
            Assert.assertTrue(route2.destinationAddressPrefix().equalsIgnoreCase(route2AddressPrefix));
            Assert.assertTrue(route2.nextHopIPAddress() == null);
            Assert.assertTrue(route2.nextHopType().equals(hopType));

            // Create a subnet that references the route table
            routeTables.manager().networks().define("net" + this.testId)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withAddressSpace("10.0.0.0/22")
                .defineSubnet("subnet1")
                    .withAddressPrefix("10.0.0.0/22")
                    .withExistingRouteTable(routeTable)
                    .attach()
                .create();

            List<Subnet> subnets = routeTable.refresh().listAssociatedSubnets();
            Assert.assertTrue(subnets.size() == 1);
            Assert.assertTrue(subnets.get(0).routeTableId().equalsIgnoreCase(routeTable.id()));
            return routeTable;
        }

        @Override
        public RouteTable updateResource(RouteTable routeTable) throws Exception {
            routeTable =  routeTable.update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .withoutRoute(ROUTE1_NAME)
                    .defineRoute(ROUTE_ADDED_NAME)
                        .withDestinationAddressPrefix("10.0.2.0/29")
                        .withNextHop(RouteNextHopType.NONE)
                        .attach()
                    .updateRoute(ROUTE2_NAME)
                        .withDestinationAddressPrefix("50.46.112.0/29")
                        .withNextHop(RouteNextHopType.INTERNET)
                        .parent()
                    .withRouteViaVirtualAppliance("10.0.5.0/29", VIRTUAL_APPLIANCE_IP)
                    .apply();

            Assert.assertTrue(routeTable.tags().containsKey("tag1"));
            Assert.assertTrue(routeTable.tags().containsKey("tag2"));
            Assert.assertTrue(!routeTable.routes().containsKey(ROUTE1_NAME));
            Assert.assertTrue(routeTable.routes().containsKey(ROUTE2_NAME));
            Assert.assertTrue(routeTable.routes().containsKey(ROUTE_ADDED_NAME));

            routeTable.manager().networks().getByResourceGroup(routeTable.resourceGroupName(), "net" + this.testId).update()
                .updateSubnet("subnet1")
                    .withoutRouteTable()
                        .parent()
                .apply();

            List<Subnet> subnets = routeTable.refresh().listAssociatedSubnets();
            Assert.assertTrue(subnets.size() == 0);

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
                .append("\n\t\t\tNext hop IP address: ").append(route.nextHopIPAddress());
        }

        // Output associated subnets
        List<Subnet> subnets = resource.listAssociatedSubnets();
        info.append("\n\tAssociated subnets: ").append(subnets.size());
        for (Subnet subnet : subnets) {
            info.append("\n\t\tResource group: ").append(subnet.parent().resourceGroupName())
                .append("\n\t\tNetwork name: ").append(subnet.parent().name())
                .append("\n\t\tSubnet name: ").append(subnet.name())
                .append("\n\tSubnet's route table ID: ").append(subnet.routeTableId());
        }

        System.out.println(info.toString());
    }
}
