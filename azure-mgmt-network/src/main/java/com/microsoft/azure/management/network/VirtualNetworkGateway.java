/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;


import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayInner;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Virtual Network Gateway management API in Azure.
 */
@Fluent
@Beta
public interface VirtualNetworkGateway extends
        GroupableResource<NetworkManager, VirtualNetworkGatewayInner>,
        Refreshable<VirtualNetworkGateway>,
        Updatable<VirtualNetworkGateway.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * The entirety of the virtual network gateway definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithGatewayType,
            DefinitionStages.WithVPNType,
            DefinitionStages.WithSku,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual network definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual network definition.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the virtual network definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithGatewayType> {
        }

        /**
         * The stage of virtual network gateway definition allowing to specify virtual network gateway type.
         */
        interface WithGatewayType {
            DefinitionStages.WithPublicIPAddress withExpressRoute();
            DefinitionStages.WithVPNType withVPN();
        }

        /**
         * The stage of virtual network gateway definition allowing to specify virtual network gateway type.
         */
        interface WithVPNType {
            DefinitionStages.WithSku withRouteBased();
            DefinitionStages.WithSku withPolicyBased();
        }

        interface WithSku {
            DefinitionStages.WithCreate withSku();
        }

        /**
         * The stage of virtual network gateway definition allowing to specify public IP address for IP configuration.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddressNoDnsLabel<VirtualNetworkGateway.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the virtual network definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified, except for adding subnets.
         */
        interface WithCreate extends
                Creatable<VirtualNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithPublicIPAddress {
        }
    }

    /**
     * Grouping of virtual network gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of virtual network gateway update allowing to specify public IP address for IP configuration.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.UpdateStages.WithPublicIPAddressNoDnsLabel<Update> {
        }
    }

    /**
     * The template for a virtual network update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<VirtualNetworkGateway>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithPublicIPAddress {
    }
}
