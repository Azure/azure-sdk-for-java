/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;


import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayInner;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Completable;
import rx.Observable;

import java.util.Collection;

/**
 * Entry point for Virtual Network Gateway management API in Azure.
 */
@Fluent
@Beta
public interface VirtualNetworkGateway extends
        GroupableResource<NetworkManager, VirtualNetworkGatewayInner>,
        Refreshable<VirtualNetworkGateway>,
        Updatable<VirtualNetworkGateway.Update> {

    // Actions

    /**
     * Resets the primary of the virtual network gateway.
     */
    @Method
    void reset();

    /**
     * Resets the primary of the virtual network gateway asynchronously.
     * @return a representation of the deferred computation of this call
     */
    @Method
    Completable resetAsync();

    /**
     * @return all the connections associated with this virtual network gateway
     */
    @Method
    PagedList<VirtualNetworkGatewayConnection> listConnections();

    /**
     * Get all the connections associated with this virtual network gateway asynchronously.
     * @return all the connections associated with this virtual network gateway
     */
    @Method
    Observable<VirtualNetworkGatewayConnection> listConnectionsAsync();

    /**
     * @return the entry point to virtual network gateway connections management API for this virtual network gateway
     */
    VirtualNetworkGatewayConnections connections();

    // Getters

    /**
     * @return the gatewayType value
     */
    VirtualNetworkGatewayType gatewayType();

    /**
     * @return the type of this virtual network gateway
     */
    VpnType vpnType();

    /**
     * @return whether BGP is enabled for this virtual network gateway or not
     */
    boolean isBgpEnabled();

    /**
     * @return activeActive flag
     */
    boolean activeActive();

    /**
     * @return the resource id of the LocalNetworkGateway resource which represents local network site having default routes
     */
    String gatewayDefaultSiteResourceId();

    /**
     * @return the SKU of this virtual network gateway
     */
    VirtualNetworkGatewaySku sku();

    /**
     * @return the reference of the VpnClientConfiguration resource which represents the P2S VpnClient configurations
     */
    VpnClientConfiguration vpnClientConfiguration();

    /**
     * @return virtual network gateway's BGP speaker settings
     */
    BgpSettings bgpSettings();

    /**
     * @return IP configurations for virtual network gateway
     */
    Collection<VirtualNetworkGatewayIPConfiguration> ipConfigurations();

    /**
     * The entirety of the virtual network gateway definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithGatewayType,
            DefinitionStages.WithSku,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithBgp,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual network gateway definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual network gateway definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the virtual network gateway definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithNetwork> {
        }

        /**
         * The stage of virtual network gateway definition allowing to specify virtual network gateway type.
         */
        interface WithGatewayType {
            /**
             * Use Express route gateway type.
             * @return the next stage of the definition
             */
            WithPublicIPAddress withExpressRoute();
            /**
             * Use Route-based VPN type.
             * @return the next stage of the definition
             */
            WithSku withRouteBasedVpn();
            /**
             * Use Policy-based VPN type. Note: this is available only for Basic SKU.
             * @return the next stage of the definition
             */
            WithCreate withPolicyBasedVpn();
        }

        /**
         * The stage of the virtual network gateway definition allowing to specify the virtual network.
         */
        interface WithNetwork {
            /**
             * Create a new virtual network to associate with the virtual network gateway,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the virtual network gateway.
             * the virtual network will be created in the same resource group and region as of parent
             * virtual network gateway, it will be created with the specified address space and a subnet for virtual network gateway.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for the virtual network
             * @param subnetAddressSpaceCidr the address space for the subnet
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(String name, String addressSpace, String subnetAddressSpaceCidr);

            /**
             * Creates a new virtual network to associate with the virtual network gateway.
             * the virtual network will be created in the same resource group and region as of parent virtual network gateway,
             * it will be created with the specified address space and a default subnet for virtual network gateway.
             *
             * @param addressSpaceCidr the address space for the virtual network
             * @param subnetAddressSpaceCidr the address space for the subnet
             * @return the next stage of the definition
             */
            WithGatewayType withNewNetwork(String addressSpaceCidr, String subnetAddressSpaceCidr);

            /**
             * Associate an existing virtual network with the virtual network gateway .
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithGatewayType withExistingNetwork(Network network);
        }

        /**
         * The stage of virtual network gateway definition allowing to specify SKU.
         */
        interface WithSku {
            WithCreate withSku(VirtualNetworkGatewaySkuName skuName);
        }

        /**
         * The stage of virtual network gateway definition allowing to specify public IP address for IP configuration.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddressNoDnsLabel<VirtualNetworkGateway.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of definition allowing to specify virtual network gateway's BGP speaker settings.
         * Note: BGP is supported on Route-Based VPN gateways only.
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
         * The stage of the virtual network gateway definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithPublicIPAddress,
                DefinitionStages.WithBgp {
        }
    }

    /**
     * Grouping of virtual network gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of virtual network gateway update allowing to change SKU.
         */
        interface WithSku {
            Update withSku(VirtualNetworkGatewaySkuName skuName);
        }

        /**
         * The stage of update allowing to specify virtual network gateway's BGP speaker settings.
         * Note: BGP is supported on Route-Based VPN gateways only.
         */
        interface WithBgp {
            /**
             * Enables BGP.
             * @param asn the BGP speaker's ASN
             * @param bgpPeeringAddress the BGP peering address and BGP identifier of this BGP speaker
             * @return the next stage of the update
             */
            Update withBgp(long asn, String bgpPeeringAddress);

            /**
             * Disables BGP for this virtual network gateway.
             * @return the next stage of the update
             */
            Update withoutBgp();
        }
    }

    /**
     * The template for a virtual network gateway update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<VirtualNetworkGateway>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithBgp {
    }
}
