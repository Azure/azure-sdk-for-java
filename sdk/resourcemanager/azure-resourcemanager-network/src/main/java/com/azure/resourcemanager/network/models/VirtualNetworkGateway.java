// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkGatewayInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Collection;
import reactor.core.publisher.Mono;

/** Entry point for Virtual Network Gateway management API in Azure. */
@Fluent
public interface VirtualNetworkGateway
    extends GroupableResource<NetworkManager, VirtualNetworkGatewayInner>,
        Refreshable<VirtualNetworkGateway>,
        Updatable<VirtualNetworkGateway.Update>,
        UpdatableWithTags<VirtualNetworkGateway> {

    // Actions

    /** Resets the primary of the virtual network gateway. */
    void reset();

    /**
     * Resets the primary of the virtual network gateway asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> resetAsync();

    /** @return all the connections associated with this virtual network gateway */
    PagedIterable<VirtualNetworkGatewayConnection> listConnections();

    /**
     * Get all the connections associated with this virtual network gateway asynchronously.
     *
     * @return all the connections associated with this virtual network gateway
     */
    PagedFlux<VirtualNetworkGatewayConnection> listConnectionsAsync();

    /**
     * Generates VPN profile for P2S client of the virtual network gateway in the specified resource group. Used for
     * IKEV2 and radius based authentication.
     *
     * @return String object if successful
     */
    String generateVpnProfile();

    /**
     * Generates asynchronously VPN profile for P2S client of the virtual network gateway in the specified resource
     * group. Used for IKEV2 and radius based authentication.
     *
     * @return String object if successful
     */
    Mono<String> generateVpnProfileAsync();

    /**
     * @return the entry point to virtual network gateway connections management API for this virtual network gateway
     */
    VirtualNetworkGatewayConnections connections();

    // Getters

    /** @return the gatewayType value */
    VirtualNetworkGatewayType gatewayType();

    /** @return the type of this virtual network gateway */
    VpnType vpnType();

    /** @return whether BGP is enabled for this virtual network gateway or not */
    boolean isBgpEnabled();

    /** @return activeActive flag */
    boolean activeActive();

    /**
     * @return the resource id of the LocalNetworkGateway resource which represents local network site having default
     *     routes
     */
    String gatewayDefaultSiteResourceId();

    /** @return the SKU of this virtual network gateway */
    VirtualNetworkGatewaySku sku();

    /**
     * @return the reference of the VpnClientConfiguration resource which represents the P2S VpnClient configurations
     */
    VpnClientConfiguration vpnClientConfiguration();

    /** @return virtual network gateway's BGP speaker settings */
    BgpSettings bgpSettings();

    /** @return IP configurations for virtual network gateway */
    Collection<VirtualNetworkGatewayIpConfiguration> ipConfigurations();

    /** The entirety of the virtual network gateway definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithGatewayType,
            DefinitionStages.WithSku,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithBgp,
            DefinitionStages.WithCreate {
    }

    /** Grouping of virtual network gateway definition stages. */
    interface DefinitionStages {
        /** The first stage of a virtual network gateway definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the virtual network gateway definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithNetwork> {
        }

        /** The stage of virtual network gateway definition allowing to specify virtual network gateway type. */
        interface WithGatewayType {
            /**
             * Use Express route gateway type.
             *
             * @return the next stage of the definition
             */
            WithSku withExpressRoute();

            /**
             * Use Route-based VPN type.
             *
             * @return the next stage of the definition
             */
            WithSku withRouteBasedVpn();

            /**
             * Use Policy-based VPN type. Note: this is available only for Basic SKU.
             *
             * @return the next stage of the definition
             */
            WithCreate withPolicyBasedVpn();
        }

        /** The stage of the virtual network gateway definition allowing to specify the virtual network. */
        interface WithNetwork {
            /**
             * Create a new virtual network to associate with the virtual network gateway, based on the provided
             * definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the virtual network gateway. the virtual network will be
             * created in the same resource group and region as of parent virtual network gateway, it will be created
             * with the specified address space and a subnet for virtual network gateway.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for the virtual network
             * @param subnetAddressSpaceCidr the address space for the subnet
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(String name, String addressSpace, String subnetAddressSpaceCidr);

            /**
             * Creates a new virtual network to associate with the virtual network gateway. the virtual network will be
             * created in the same resource group and region as of parent virtual network gateway, it will be created
             * with the specified address space and a default subnet for virtual network gateway.
             *
             * @param addressSpaceCidr the address space for the virtual network
             * @param subnetAddressSpaceCidr the address space for the subnet
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(String addressSpaceCidr, String subnetAddressSpaceCidr);

            /**
             * Associate an existing virtual network with the virtual network gateway.
             *
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithGatewayType withExistingNetwork(Network network);
        }

        /** The stage of virtual network gateway definition allowing to specify SKU. */
        interface WithSku {
            WithCreate withSku(VirtualNetworkGatewaySkuName skuName);
        }

        /**
         * The stage of virtual network gateway definition allowing to specify public IP address for IP configuration.
         */
        interface WithPublicIPAddress
            extends HasPublicIpAddress.DefinitionStages.WithPublicIPAddressNoDnsLabel<WithCreate> {
        }

        /**
         * The stage of definition allowing to specify virtual network gateway's BGP speaker settings. Note: BGP is
         * supported on Route-Based VPN gateways only.
         */
        interface WithBgp {
            /**
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             * @return the next stage of the definition
             */
            WithCreate withBgp(long asn, String bgpPeeringAddress);
        }

        /**
         * The stage of the virtual network gateway definition which contains all the minimum required inputs for the
         * resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<VirtualNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithPublicIPAddress,
                DefinitionStages.WithBgp {
        }
    }

    /** Grouping of virtual network gateway update stages. */
    interface UpdateStages {
        /** The stage of virtual network gateway update allowing to change SKU. */
        interface WithSku {
            Update withSku(VirtualNetworkGatewaySkuName skuName);
        }

        /**
         * The stage of update allowing to specify virtual network gateway's BGP speaker settings. Note: BGP is
         * supported on Route-Based VPN gateways only.
         */
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
             * Disables BGP for this virtual network gateway.
             *
             * @return the next stage of the update
             */
            Update withoutBgp();
        }

        /** The stage of update allowing to specify virtual network gateway's point-to-site configuration. */
        interface WithPointToSiteConfiguration {

            /**
             * Begins the definition of point-to-site configuration to be added to this virtual network gateway.
             *
             * @return the first stage of the point-to-site configuration definition
             */
            PointToSiteConfiguration.DefinitionStages.Blank<Update> definePointToSiteConfiguration();

            PointToSiteConfiguration.Update updatePointToSiteConfiguration();
        }
    }

    /**
     * The template for a virtual network gateway update operation, containing all the settings that can be modified.
     */
    interface Update
        extends Appliable<VirtualNetworkGateway>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithBgp,
            UpdateStages.WithPointToSiteConfiguration {
    }
}
