/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.LocalNetworkGatewayInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Local Network Gateway management API in Azure.
 */
@Fluent
@Beta
public interface LocalNetworkGateway extends
        GroupableResource<NetworkManager, LocalNetworkGatewayInner>,
        Refreshable<LocalNetworkGateway>,
        Updatable<LocalNetworkGateway.Update> {

    // Getters

    /**
     * @return local network gateway's BGP speaker settings
     */
    BgpSettings bgpSettings();

    /**
     * The entirety of the local network gateway definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithIPAddress,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of local network gateway definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a local network gateway definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the local network gateway definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithIPAddress> {
        }

        interface WithIPAddress {
            WithCreate withIPAddress(String ipAddress);
        }

        /**
         * The stage of definition allowing to specify local network gateway's BGP speaker settings.
         * Note: BGP is supported on Route-Based VPN gateways only.
         */
        interface WithBgpSettingsAndCreate extends WithCreate {

        }

        /**
         * The stage of the local network gateway definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}).
         */
        interface WithCreate extends
                Creatable<LocalNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of local network gateway update stages.
     */
    interface UpdateStages {
        interface WithBgpSettings {

        }
    }

    /**
     * The template for a local network gateway update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<LocalNetworkGateway>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithBgpSettings {
    }
}
