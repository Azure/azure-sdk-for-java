/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;


import com.azure.management.network.Route;
import com.azure.management.network.RouteNextHopType;
import com.azure.management.network.RouteTable;
import com.azure.management.network.RouteTables;
import com.azure.management.network.Subnet;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

/**
 * Test of virtual network management.
 */
public class TestRouteTables {
    private String ROUTE1_NAME = "route1";
    private String ROUTE2_NAME = "route2";
    private String ROUTE_ADDED_NAME = "route3";
    private String VIRTUAL_APPLIANCE_IP = "10.1.1.1";

    /**
     * Test of minimal route tables.
     */
    public class Minimal extends TestTemplate<RouteTable, RouteTables> {
        private String netName;
        @Override
        public RouteTable createResource(RouteTables routeTables) throws Exception {
            netName = routeTables.manager().getSdkContext().randomResourceName("net", 10);
            final String newName = routeTables.manager().getSdkContext().randomResourceName("rt", 10);;
            Region region = Region.US_WEST;
            String groupName = routeTables.manager().getSdkContext().randomResourceName("rg", 10);;

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
                    .withDisableBgpRoutePropagation()
                    .create();

            Assertions.assertTrue(routeTable.routes().containsKey(ROUTE1_NAME));
            Route route1 = routeTable.routes().get(ROUTE1_NAME);
            Assertions.assertTrue(route1.destinationAddressPrefix().equalsIgnoreCase(route1AddressPrefix));
            Assertions.assertTrue(route1.nextHopIPAddress().equalsIgnoreCase(VIRTUAL_APPLIANCE_IP));
            Assertions.assertTrue(route1.nextHopType().equals(RouteNextHopType.VIRTUAL_APPLIANCE));

            Assertions.assertTrue(routeTable.routes().containsKey(ROUTE2_NAME));
            Route route2 = routeTable.routes().get(ROUTE2_NAME);
            Assertions.assertTrue(route2.destinationAddressPrefix().equalsIgnoreCase(route2AddressPrefix));
            Assertions.assertTrue(route2.nextHopIPAddress() == null);
            Assertions.assertTrue(route2.nextHopType().equals(hopType));

            Assertions.assertTrue(routeTable.isBgpRoutePropagationDisabled());

            // Create a subnet that references the route table
            routeTables.manager().networks().define(netName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    .withAddressSpace("10.0.0.0/22")
                    .defineSubnet("subnet1")
                    .withAddressPrefix("10.0.0.0/22")
                    .withExistingRouteTable(routeTable)
                    .attach()
                    .create();

            List<Subnet> subnets = routeTable.refresh().listAssociatedSubnets();
            Assertions.assertTrue(subnets.size() == 1);
            Assertions.assertTrue(subnets.get(0).routeTableId().equalsIgnoreCase(routeTable.id()));
            return routeTable;
        }

        @Override
        public RouteTable updateResource(RouteTable routeTable) throws Exception {
            routeTable = routeTable.update()
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
                    .withEnableBgpRoutePropagation()
                    .apply();

            Assertions.assertTrue(routeTable.tags().containsKey("tag1"));
            Assertions.assertTrue(routeTable.tags().containsKey("tag2"));
            Assertions.assertTrue(!routeTable.routes().containsKey(ROUTE1_NAME));
            Assertions.assertTrue(routeTable.routes().containsKey(ROUTE2_NAME));
            Assertions.assertTrue(routeTable.routes().containsKey(ROUTE_ADDED_NAME));
            Assertions.assertFalse(routeTable.isBgpRoutePropagationDisabled());

            routeTable.manager().networks().getByResourceGroup(routeTable.resourceGroupName(), netName).update()
                    .updateSubnet("subnet1")
                    .withoutRouteTable()
                    .parent()
                    .apply();

            List<Subnet> subnets = routeTable.refresh().listAssociatedSubnets();
            Assertions.assertTrue(subnets.size() == 0);

            routeTable.updateTags()
                    .withoutTag("tag1")
                    .withTag("tag3", "value3")
                    .applyTags();
            Assertions.assertFalse(routeTable.tags().containsKey("tag1"));
            Assertions.assertEquals("value3", routeTable.tags().get("tag3"));
            return routeTable;
        }

        @Override
        public void print(RouteTable resource) {
            printRouteTable(resource);
        }
    }

    /**
     * Outputs info about a route table
     *
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
        info.append("\n\tDisable BGP route propagation: ").append(resource.isBgpRoutePropagationDisabled());
        System.out.println(info.toString());
    }
}
