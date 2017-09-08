/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;


import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayIPConfigurationInner;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayInner;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;

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
    Boolean enableBgp();

    /**
     * @return activeActive flag
     */
    Boolean activeActive();

    /**
     * @return the reference of the LocalNetworkGateway resource which represents local network site having default routes
     */
    SubResource gatewayDefaultSite();

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
    List<VirtualNetworkGatewayIPConfigurationInner> ipConfigurations();

    /**
     * The entirety of the virtual network gateway definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithGatewayType,
            DefinitionStages.WithVPNType,
            DefinitionStages.WithSku,
            DefinitionStages.WithNetwork,
            DefinitionStages.WithCreate,
            DefinitionStages.WithBgpSettingsAndCreate {
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
            /**
             * Use Route-based VPN type.
             * @return the next stage of the definition
             */
            WithSku withRouteBased();

            /**
             * Use Policy-based VPN type. Note: this is available only for Basic SKU.
             * @return the next stage of the definition
             */
            DefinitionStages.WithNetwork withPolicyBased();
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
            DefinitionStages.WithCreate withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the virtual network gateway.
             * the virtual network will be created in the same resource group and region as of parent
             * virtual network gateway, it will be created with the specified address space and a subnet for virtual network gateway.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withNewNetwork(String name, String addressSpace, String subnetAddressSpaceCidr);

            /**
             * Creates a new virtual network to associate with the virtual network gateway.
             * the virtual network will be created in the same resource group and region as of parent virtual network gateway,
             * it will be created with the specified address space and a default subnet for virtual network gateway.
             *
             * @param addressSpaceCidr the address space for the virtual network
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withNewNetwork(String addressSpaceCidr, String subnetAddressSpaceCidr);

            /**
             * Associate an existing virtual network with the virtual network gateway .
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            DefinitionStages.WithCreate withExistingNetwork(Network network);
        }

        /**
         * The stage of virtual network gateway definition allowing to specify SKU.
         */
        interface WithSku {
            DefinitionStages.WithNetwork withSku(VirtualNetworkGatewaySkuName skuName);
        }

        /**
         * The stage of virtual network gateway definition allowing to specify public IP address for IP configuration.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddressNoDnsLabel<VirtualNetworkGateway.DefinitionStages.WithCreate> {
        }

        interface WithActiveActive {
            DefinitionStages.WithCreate withActiveActive(boolean activeActive);
        }

        /**
         * The stage of definition allowing to specify virtual network gateway's BGP speaker settings.
         * Note: BGP is supported on Route-Based VPN gateways only.
         */
        interface WithBgpSettingsAndCreate extends WithCreate {

        }

        /**
         * The stage of the virtual network gateway definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<VirtualNetworkGateway>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithPublicIPAddress,
                DefinitionStages.WithActiveActive {
        }
    }

    /**
     * Grouping of virtual network gateway update stages.
     */
    interface UpdateStages {
        /**
         * The stage of virtual network gateway definition allowing to specify SKU.
         */
        interface WithSku {
            Update withSku(VirtualNetworkGatewaySkuName skuName);
        }

        interface WithBgpSettings {

        }

        /**
         * The stage of an application gateway update allowing to modify probes.
         */
        interface WithConnection {
            /**
             * Begins the definition of a new connection.
             * @param name a unique name for the connection
             * @return the first stage of virtual network gateway connection definition
             */
            VirtualNetworkGatewayConnection.DefinitionStages.Blank defineConnection(String name);

            /**
             * Begins the update of an existing probe.
             * @param name the name of an existing probe
             * @return the first stage of a probe update
             */
            ApplicationGatewayProbe.Update updateProbe(String name);

            /**
             * Removes a probe from the application gateway.
             * <p>
             * Any references to this probe from backend HTTP configurations will be automatically removed.
             * @param name the name of an existing probe
             * @return the next stage of the update
             */
            Update withoutProbe(String name);
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
            UpdateStages.WithBgpSettings {
    }
}
