/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.RouteTableInner;
import com.microsoft.azure.management.network.model.HasAssociatedSubnets;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.Map;

/**
 * Entry point for route table management.
 */
@Fluent()
public interface RouteTable extends
        GroupableResource,
        Refreshable<RouteTable>,
        Wrapper<RouteTableInner>,
        Updatable<RouteTable.Update>,
        HasAssociatedSubnets {

    /**
     * @return the routes of this route table
     */
    Map<String, Route> routes();

    /**
     * The entirety of a route table definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of route table definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a route table definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of a route table definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the route table definition allowing to add routes.
         */
        interface WithRoute {
            /**
             * Begins the definition of a new route to add to the route table.
             * <p>
             * The definition must be completed with a call to {@link Route.DefinitionStages.WithAttach#attach()}
             * @param name the name of the route
             * @return the first stage of the definition
             */
            Route.DefinitionStages.Blank<WithCreate> defineRoute(String name);

            /**
             * Creates a non-virtual appliance route.
             * <p>
             * The name is generated automatically.
             * @param destinationAddressPrefix the destination address prefix, expressed in the CIDR notation, for the route to apply to
             * @param nextHop the next hop type
             * @return the next stage of the definition
             */
            WithCreate withRoute(String destinationAddressPrefix, RouteNextHopType nextHop);

            /**
             * Creates a route via a virtual appliance.
             * @param destinationAddressPrefix the destination address prefix, expressed in the CIDR notation, for the route to apply to
             * @param ipAddress the IP address of the virtual appliance to route the traffic through
             * @return the next stage of the definition
             */
            WithCreate withRouteViaVirtualAppliance(String destinationAddressPrefix, String ipAddress);
        }

        /**
         * The stage of a route table definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<RouteTable>,
            Resource.DefinitionWithTags<WithCreate>,
            DefinitionStages.WithRoute {
        }
    }

    /**
     * Grouping of route table update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the route table definition allowing to add, remove or modify routes.
         */
        interface WithRoute {
            /**
             * Creates a non-virtual appliance route.
             * <p>
             * The name is generated automatically.
             * @param destinationAddressPrefix the destination address prefix, expressed in the CIDR notation, for the route to apply to
             * @param nextHop the next hop type
             * @return the next stage of the update
             */
            Update withRoute(String destinationAddressPrefix, RouteNextHopType nextHop);

            /**
             * Creates a route via a virtual appliance.
             * @param destinationAddressPrefix the destination address prefix, expressed in the CIDR notation, for the route to apply to
             * @param ipAddress the IP address of the virtual appliance to route the traffic through
             * @return the next stage of the update
             */
            Update withRouteViaVirtualAppliance(String destinationAddressPrefix, String ipAddress);

            /**
             * Begins the definition of a new route to add to the route table.
             * <p>
             * The definition must be completed with a call to {@link Route.UpdateDefinitionStages.WithAttach#attach()}
             * @param name the name of the route
             * @return the first stage of the definition
             */
            Route.UpdateDefinitionStages.Blank<Update> defineRoute(String name);

            /**
             * Removes the specified route from the route table.
             * @param name the name of an existing route on this route table
             * @return the next stage of the update
             */
            Update withoutRoute(String name);

            /**
             * Begins the update of an existing route on this route table.
             * @param name the name of an existing route
             * @return the first stage of the update
             */
            Route.Update updateRoute(String name);
        }
    }

    /**
     * The template for a route table update operation, containing all the settings that can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<RouteTable>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithRoute {
    }
}
