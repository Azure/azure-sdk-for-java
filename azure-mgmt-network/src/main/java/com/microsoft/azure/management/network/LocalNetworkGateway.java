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

import java.util.Set;

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
     * @return IP address of local network gateway
     */
    String ipAddress();

    /**
     * @return local network gateway's BGP speaker settings
     */
    BgpSettings bgpSettings();

    /**
     * @return local network site address spaces
     */
    Set<String> addressSpaces();

    /**
     * @return the provisioning state of the LocalNetworkGateway resource
     */
    String provisioningState();

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

        /**
         * The stage of the local network gateway definition allowing to specify IP address of local network gateway.
         */
        interface WithIPAddress {
            WithAddressSpace withIPAddress(String ipAddress);
        }

        /**
         * The stage of the local network gateway definition allowing to specify the address space.
         */
        interface WithAddressSpace {
            /**
             * Adds address space.
             * Note: this method's effect is additive, i.e. each time it is used, a new address space is added to the network.
             * @param cidr the CIDR representation of the local network site address space
             */
            WithCreate withAddressSpace(String cidr);
        }

        /**
         * The stage of definition allowing to specify local network gateway's BGP speaker settings.
         */
        interface WithBgp {
            /**
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             */
            WithCreate withBgp(long asn, String bgpPeeringAddress);
        }

        /**
         * The stage of the local network gateway definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}).
         */
        interface WithCreate extends
                Creatable<LocalNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithAddressSpace,
                DefinitionStages.WithBgp {
        }
    }

    /**
     * Grouping of local network gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the local network gateway update allowing to change IP address of local network gateway.
         */
        interface WithIPAddress {
            Update withIPAddress(String ipAddress);
        }

        /**
         * The stage of the local network gateway update allowing to specify the address spaces.
         */
        interface WithAddressSpace {
            /**
             * Adds address space.
             * Note: this method's effect is additive, i.e. each time it is used, a new address space is added to the network.
             * @param cidr the CIDR representation of the local network site address space.
             */
            Update withAddressSpace(String cidr);

            /**
             * Remove address space. Note: address space will be removed only in case of exact cidr string match.
             * @param cidr the CIDR representation of the local network site address space.
             */
            Update withoutAddressSpace(String cidr);
        }

        /**
         * The stage of update allowing to specify local network gateway's BGP speaker settings.
         */
        interface WithBgp {
            /**
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             */
            Update withBgp(long asn, String bgpPeeringAddress);

            Update disableBgp();
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
            UpdateStages.WithIPAddress,
            UpdateStages.WithAddressSpace,
            UpdateStages.WithBgp {
    }
}
