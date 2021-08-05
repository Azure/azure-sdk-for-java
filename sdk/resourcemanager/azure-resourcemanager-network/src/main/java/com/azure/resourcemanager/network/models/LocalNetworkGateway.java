// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.LocalNetworkGatewayInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Set;

/** Entry point for Local Network Gateway management API in Azure. */
@Fluent
public interface LocalNetworkGateway
    extends GroupableResource<NetworkManager, LocalNetworkGatewayInner>,
        Refreshable<LocalNetworkGateway>,
        Updatable<LocalNetworkGateway.Update>,
        UpdatableWithTags<LocalNetworkGateway> {

    // Getters

    /** @return IP address of local network gateway */
    String ipAddress();

    /** @return local network gateway's BGP speaker settings */
    BgpSettings bgpSettings();

    /** @return local network site address spaces */
    Set<String> addressSpaces();

    /** @return the provisioning state of the LocalNetworkGateway resource */
    String provisioningState();

    /** The entirety of the local network gateway definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithIPAddress,
            DefinitionStages.WithCreate {
    }

    /** Grouping of local network gateway definition stages. */
    interface DefinitionStages {
        /** The first stage of a local network gateway definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the local network gateway definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithIPAddress> {
        }

        /**
         * The stage of the local network gateway definition allowing to specify IP address of local network gateway.
         */
        interface WithIPAddress {
            /**
             * Specifies the IP address of the local network gateway.
             *
             * @param ipAddress an IP address
             * @return the next stage of the definition
             */
            WithAddressSpace withIPAddress(String ipAddress);
        }

        /** The stage of the local network gateway definition allowing to specify the address space. */
        interface WithAddressSpace {
            /**
             * Adds address space. Note: this method's effect is additive, i.e. each time it is used, a new address
             * space is added to the network.
             *
             * @param cidr the CIDR representation of the local network site address space
             * @return the next stage of the definition
             */
            WithCreate withAddressSpace(String cidr);
        }

        /** The stage of definition allowing to specify local network gateway's BGP speaker settings. */
        interface WithBgp {
            /**
             * Enables BGP.
             *
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             * @return the next stage of the definition
             */
            WithCreate withBgp(long asn, String bgpPeeringAddress);
        }

        /**
         * The stage of the local network gateway definition which contains all the minimum required inputs for the
         * resource to be created (via {@link WithCreate#create()}).
         */
        interface WithCreate
            extends Creatable<LocalNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithAddressSpace,
                DefinitionStages.WithBgp {
        }
    }

    /** Grouping of local network gateway update stages. */
    interface UpdateStages {
        /** The stage of the local network gateway update allowing to change IP address of local network gateway. */
        interface WithIPAddress {
            Update withIPAddress(String ipAddress);
        }

        /** The stage of the local network gateway update allowing to specify the address spaces. */
        interface WithAddressSpace {
            /**
             * Adds address space. Note: this method's effect is additive, i.e. each time it is used, a new address
             * space is added to the network.
             *
             * @param cidr the CIDR representation of the local network site address space
             * @return the next stage of the update
             */
            Update withAddressSpace(String cidr);

            /**
             * Remove address space. Note: address space will be removed only in case of exact cidr string match.
             *
             * @param cidr the CIDR representation of the local network site address space
             * @return the next stage of the update
             */
            Update withoutAddressSpace(String cidr);
        }

        /** The stage of update allowing to specify local network gateway's BGP speaker settings. */
        interface WithBgp {
            /**
             * Enables BGP.
             *
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             * @return the next stage of the update
             */
            Update withBgp(long asn, String bgpPeeringAddress);

            /**
             * Disables BGP.
             *
             * @return the next stage of the update
             */
            Update withoutBgp();
        }
    }

    /** The template for a local network gateway update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<LocalNetworkGateway>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithIPAddress,
            UpdateStages.WithAddressSpace,
            UpdateStages.WithBgp {
    }
}
