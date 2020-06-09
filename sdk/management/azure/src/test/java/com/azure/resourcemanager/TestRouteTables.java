// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.Route;
import com.azure.resourcemanager.network.models.RouteNextHopType;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.models.RouteTables;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

/** Test of virtual network management. */
public class TestRouteTables {
    private String route1Name = "route1";
    private String route2Name = "route2";
    private String routeAddedName = "route3";
    private String virtualApplianceIp = "10.1.1.1";

    /** Test of minimal route tables. */
    public class Minimal extends TestTemplate<RouteTable, RouteTables> {
        private String netName;

        @Override
        public RouteTable createResource(RouteTables routeTables) throws Exception {
            netName = routeTables.manager().sdkContext().randomResourceName("net", 10);
            final String newName = routeTables.manager().sdkContext().randomResourceName("rt", 10);

            Region region = Region.US_WEST;
            String groupName = routeTables.manager().sdkContext().randomResourceName("rg", 10);

            final String route1AddressPrefix = "10.0.1.0/29";
            final String route2AddressPrefix = "10.0.0.0/29";
            final RouteNextHopType hopType = RouteNextHopType.VNET_LOCAL;

            // Create a route table
            final RouteTable routeTable =
                routeTables
                    .define(newName)
                    .withRegion(region)
                    .withNewResourceGroup(groupName)
                    .withRoute("10.0.3.0/29", RouteNextHopType.VNET_LOCAL)
                    .defineRoute(route1Name)
                    .withDestinationAddressPrefix(route1AddressPrefix)
                    .withNextHopToVirtualAppliance(virtualApplianceIp)
                    .attach()
                    .defineRoute(route2Name)
                    .withDestinationAddressPrefix(route2AddressPrefix)
                    .withNextHop(hopType)
                    .attach()
                    .withDisableBgpRoutePropagation()
                    .create();

            Assertions.assertTrue(routeTable.routes().containsKey(route1Name));
            Route route1 = routeTable.routes().get(route1Name);
            Assertions.assertTrue(route1.destinationAddressPrefix().equalsIgnoreCase(route1AddressPrefix));
            Assertions.assertTrue(route1.nextHopIpAddress().equalsIgnoreCase(virtualApplianceIp));
            Assertions.assertTrue(route1.nextHopType().equals(RouteNextHopType.VIRTUAL_APPLIANCE));

            Assertions.assertTrue(routeTable.routes().containsKey(route2Name));
            Route route2 = routeTable.routes().get(route2Name);
            Assertions.assertTrue(route2.destinationAddressPrefix().equalsIgnoreCase(route2AddressPrefix));
            Assertions.assertTrue(route2.nextHopIpAddress() == null);
            Assertions.assertTrue(route2.nextHopType().equals(hopType));

            Assertions.assertTrue(routeTable.isBgpRoutePropagationDisabled());

            // Create a subnet that references the route table
            routeTables
                .manager()
                .networks()
                .define(netName)
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
            routeTable =
                routeTable
                    .update()
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .withoutRoute(route1Name)
                    .defineRoute(routeAddedName)
                    .withDestinationAddressPrefix("10.0.2.0/29")
                    .withNextHop(RouteNextHopType.NONE)
                    .attach()
                    .updateRoute(route2Name)
                    .withDestinationAddressPrefix("50.46.112.0/29")
                    .withNextHop(RouteNextHopType.INTERNET)
                    .parent()
                    .withRouteViaVirtualAppliance("10.0.5.0/29", virtualApplianceIp)
                    .withEnableBgpRoutePropagation()
                    .apply();

            Assertions.assertTrue(routeTable.tags().containsKey("tag1"));
            Assertions.assertTrue(routeTable.tags().containsKey("tag2"));
            Assertions.assertTrue(!routeTable.routes().containsKey(route1Name));
            Assertions.assertTrue(routeTable.routes().containsKey(route2Name));
            Assertions.assertTrue(routeTable.routes().containsKey(routeAddedName));
            Assertions.assertFalse(routeTable.isBgpRoutePropagationDisabled());

            routeTable
                .manager()
                .networks()
                .getByResourceGroup(routeTable.resourceGroupName(), netName)
                .update()
                .updateSubnet("subnet1")
                .withoutRouteTable()
                .parent()
                .apply();

            List<Subnet> subnets = routeTable.refresh().listAssociatedSubnets();
            Assertions.assertTrue(subnets.size() == 0);

            routeTable.updateTags().withoutTag("tag1").withTag("tag3", "value3").applyTags();
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
        info
            .append("Route table: ")
            .append(resource.id())
            .append("\n\tName: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags());

        // Output routes
        Map<String, Route> routes = resource.routes();
        info.append("\n\tRoutes: ").append(routes.values().size());
        for (Route route : routes.values()) {
            info
                .append("\n\t\tName: ")
                .append(route.name())
                .append("\n\t\t\tDestination address prefix: ")
                .append(route.destinationAddressPrefix())
                .append("\n\t\t\tNext hop type: ")
                .append(route.nextHopType().toString())
                .append("\n\t\t\tNext hop IP address: ")
                .append(route.nextHopIpAddress());
        }

        // Output associated subnets
        List<Subnet> subnets = resource.listAssociatedSubnets();
        info.append("\n\tAssociated subnets: ").append(subnets.size());
        for (Subnet subnet : subnets) {
            info
                .append("\n\t\tResource group: ")
                .append(subnet.parent().resourceGroupName())
                .append("\n\t\tNetwork name: ")
                .append(subnet.parent().name())
                .append("\n\t\tSubnet name: ")
                .append(subnet.name())
                .append("\n\tSubnet's route table ID: ")
                .append(subnet.routeTableId());
        }
        info.append("\n\tDisable BGP route propagation: ").append(resource.isBgpRoutePropagationDisabled());
        System.out.println(info.toString());
    }
}
