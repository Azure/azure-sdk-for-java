/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.RouteFilterInner;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;

import java.util.Map;

/**
 * Route filter.
 */
@Fluent
public interface RouteFilter extends
        GroupableResource<NetworkManager, RouteFilterInner>,
        Refreshable<RouteFilter>,
        Updatable<RouteFilter.Update> {
    /**
     * @return rules associated with this route filter, indexed by their names
     */
    Map<String, RouteFilterRule> rules();

    /**
     * @return express route circuit peerings associated with this route filter, indexed by their names
     */
    Map<String, ExpressRouteCircuitPeering> peerings();

    /**
     * @return the provisioning state of the route filter resource
     */
    String provisioningState();

    // Fluent interfaces for creating Route Filter

    /**
     * The entirety of the aroute filter definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of route filter definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<RouteFilter>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of route filter update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the resource update allowing to add or remove route filter rules.
         */
        interface WithRule {
            /**
             * Removes an route filter rule.
             *
             * @param name the name of the route filter rule to remove
             * @return the next stage of the route filter update
             */
            Update withoutRule(String name);

            /**
             * Begins the definition of a new route filter rule to be added to this route filter.
             *
             * @param name the name of the route filter rule
             * @return the first stage of the new route filter rule definition
             */
            RouteFilterRule.UpdateDefinitionStages.Blank<Update> defineRule(String name);

            /**
             * Begins the description of an update of an existing route filter rule of this route filter.
             *
             * @param name the name of an existing route filter rule
             * @return the first stage of the route filter rule update description
             */
            RouteFilterRule.Update updateRule(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<RouteFilter>,
            UpdateStages.WithRule,
            Resource.UpdateWithTags<Update> {
    }
}
