/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;

import com.microsoft.azure.management.network.implementation.LoadBalancerInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Entry point for load balancer management API in Azure.
 */
public interface LoadBalancer extends
        GroupableResource,
        Refreshable<LoadBalancer>,
        Wrapper<LoadBalancerInner>,
        Updatable<LoadBalancer.Update> {

    // Getters
    /**
     * @return resource IDs of the public IP addresses assigned to the front end of this load balancer
     */
    List<String> publicIpAddressIds();

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of load balancer definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a load balancer definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the load balancer definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the load balancer definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<LoadBalancer>,
            Resource.DefinitionWithTags<WithCreate>,
            PublicIpAddress.WithPublicIpAddress<WithCreate> {
       }
    }

    /**
     * Grouping of load balancer update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a load balancer update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<LoadBalancer>,
        Resource.UpdateWithTags<Update> {
    }
}
