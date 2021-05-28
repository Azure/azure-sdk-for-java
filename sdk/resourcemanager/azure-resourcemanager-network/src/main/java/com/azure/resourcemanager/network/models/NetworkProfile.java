// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.NetworkProfileInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;

/** An immutable client-side representation of NetworkProfile. */
public interface NetworkProfile extends
    GroupableResource<NetworkManager, NetworkProfileInner>,
    Refreshable<NetworkProfile>,
    Updatable<NetworkProfile.Update>,
    UpdatableWithTags<NetworkProfile> {

    /**
     * Gets the containerNetworkInterfaceConfigurations property: List of chid container network interface
     * configurations.
     *
     * @return the containerNetworkInterfaceConfigurations value.
     */
    List<ContainerNetworkInterfaceConfiguration> containerNetworkInterfaceConfigurations();

    /** The entirety of the NetworkProfile definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithContainerNetworkInterfaceConfigurations,
            DefinitionStages.WithCreate {
    }
    /** The NetworkProfile definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the NetworkProfile definition.
         */
        interface Blank extends
            GroupableResource.DefinitionWithRegion<NetworkProfile.DefinitionStages.WithGroup> {
        }

        /**
         * The stage of the NetworkProfile definition allowing to specify parent resource.
         */
        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup
                <NetworkProfile.DefinitionStages.WithContainerNetworkInterfaceConfigurations> {
        }

        /**
         * The stage of the NetworkProfile definition which contains all the minimum required properties for the
         * resource to be created, but also allows for any other optional properties to be specified.
         */
        interface WithCreate
            extends Creatable<NetworkProfile>,
            Resource.DefinitionWithTags<NetworkProfile.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the NetworkProfile definition allowing to specify network interface configurations for
         * container.
         */
        interface WithContainerNetworkInterfaceConfigurations {
            /**
             * Specifies the network interface configuration for container.
             *
             * @param name the name
             * @param ipConfigName the name of ip configuration
             * @param virtualNetworkId the ID of the virtual network
             * @param subnetName the name of the subnet
             * @return the next stage
             */
            WithCreate withContainerNetworkInterfaceConfiguration(
                String name, String ipConfigName, String virtualNetworkId, String subnetName);

            /**
             * Specifies the network interface configuration for container.
             *
             * @param name the name
             * @param ipConfigName the name of ip configuration
             * @param subnet the Subnet resource
             * @return the next stage
             */
            WithCreate withContainerNetworkInterfaceConfiguration(
                String name, String ipConfigName, Subnet subnet);
        }
    }

    /** The template for NetworkProfile update. */
    interface Update extends
        Appliable<NetworkProfile>,
        Resource.UpdateWithTags<NetworkProfile.Update> {
    }
}
