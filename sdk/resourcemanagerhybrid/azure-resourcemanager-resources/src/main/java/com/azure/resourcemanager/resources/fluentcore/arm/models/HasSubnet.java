// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model's ability to reference a subnet by its name and network's ID.
 */
@Fluent
public interface HasSubnet {
    /**
     * @return the resource ID of the virtual network whose subnet is associated with this resource
     */
    String networkId();

    /**
     * @return the name of the subnet associated with this resource
     */
    String subnetName();

    /**
     * Grouping of definition stages involving associating an existing subnet with a resource.
     */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to associate a subnet with a resource.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             *
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }

    /**
     * Grouping of update stages involving associating an existing subnet with a resource.
     */
    interface UpdateStages {
        /**
         * The stage of an update allowing to associate a subnet with a resource.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             *
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update,
     * involving associating a subnet with a resource.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to associate a subnet with a resource.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             *
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }
}
