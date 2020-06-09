// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayConnectionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Collection;

/** Client-side representation of Virtual Network Gateway Connection object, associated with Virtual Network Gateway. */
@Fluent
public interface VirtualNetworkGatewayConnection
    extends IndependentChildResource<NetworkManager, VirtualNetworkGatewayConnectionInner>,
        Refreshable<VirtualNetworkGatewayConnection>,
        Updatable<VirtualNetworkGatewayConnection.Update>,
        UpdatableWithTags<VirtualNetworkGatewayConnection>,
        HasParent<VirtualNetworkGateway> {

    /**
     * Get the authorizationKey value.
     *
     * @return the authorizationKey value
     */
    String authorizationKey();

    /** @return the reference to virtual network gateway resource */
    String virtualNetworkGateway1Id();

    /** @return the reference to virtual network gateway resource. */
    String virtualNetworkGateway2Id();

    /** @return the reference to local network gateway resource */
    String localNetworkGateway2Id();

    /**
     * Get the gateway connection type.
     *
     * @return the connectionType value
     */
    VirtualNetworkGatewayConnectionType connectionType();

    /** @return the routing weight */
    int routingWeight();

    /** @return the IPSec shared key */
    String sharedKey();

    /**
     * Get the Virtual Network Gateway connection status.
     *
     * @return the connectionStatus value
     */
    VirtualNetworkGatewayConnectionStatus connectionStatus();

    /**
     * Get the tunnelConnectionStatus value.
     *
     * @return collection of all tunnels' connection health status
     */
    Collection<TunnelConnectionHealth> tunnelConnectionStatus();

    /** @return the egress bytes transferred in this connection */
    long egressBytesTransferred();

    /** @return the egress bytes transferred in this connection. */
    long ingressBytesTransferred();

    /** @return the reference to peerings resource */
    String peerId();

    /** @return the enableBgp flag */
    boolean isBgpEnabled();

    /** @return if policy-based traffic selectors enabled */
    boolean usePolicyBasedTrafficSelectors();

    /** @return the IPSec Policies to be considered by this connection */
    Collection<IpsecPolicy> ipsecPolicies();

    /** @return the provisioning state of the VirtualNetworkGatewayConnection resource */
    String provisioningState();

    /** The entirety of the virtual network gateway connection definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithConnectionType,
            DefinitionStages.WithLocalNetworkGateway,
            DefinitionStages.WithSecondVirtualNetworkGateway,
            DefinitionStages.WithSharedKey,
            DefinitionStages.WithAuthorization,
            DefinitionStages.WithCreate {
    }

    /** Grouping of virtual network gateway connection definition stages. */
    interface DefinitionStages {
        /** The first stage of virtual network gateway connection definition. */
        interface Blank extends WithConnectionType {
        }

        /** Stage of definition allowing to specify connection type. */
        interface WithConnectionType {
            /**
             * Create Site-to-Site connection.
             *
             * @return next stage of definition, allowing to specify local network gateway
             */
            WithLocalNetworkGateway withSiteToSite();

            /**
             * Create VNet-to-VNet connection.
             *
             * @return the next stage of the definition, allowing to specify virtual network gateway to connect to.
             */
            WithSecondVirtualNetworkGateway withVNetToVNet();

            /**
             * Create Express Route connection.
             *
             * @param circuitId id of Express Route circuit used for connection
             * @return next stage of definition
             */
            WithCreate withExpressRoute(String circuitId);

            /**
             * Create Express Route connection.
             *
             * @param circuit Express Route circuit used for connection
             * @return the next stage of the definition
             */
            WithCreate withExpressRoute(ExpressRouteCircuit circuit);
        }

        /** Stage of definition allowing to specify local network gateway to connect to. */
        interface WithLocalNetworkGateway {
            /**
             * @param localNetworkGateway local network gateway to connect to
             * @return the next stage of the definition
             */
            WithSharedKey withLocalNetworkGateway(LocalNetworkGateway localNetworkGateway);
        }

        /** Stage of definition allowing to specify virtual network gateway to connect to. */
        interface WithSecondVirtualNetworkGateway {
            /**
             * @param virtualNetworkGateway2 virtual network gateway to connect to
             * @return the next stage of the definition
             */
            WithSharedKey withSecondVirtualNetworkGateway(VirtualNetworkGateway virtualNetworkGateway2);
        }

        /** Stage of definition allowing to specify shared key for the connection. */
        interface WithSharedKey {
            /**
             * Specify shared key.
             *
             * @param sharedKey shared key
             * @return the next stage of the definition
             */
            WithCreate withSharedKey(String sharedKey);
        }

        /** Stage of definition allowing to enable BGP for the connection. */
        interface WithBgp {
            /**
             * Enable BGP for the connection.
             *
             * @return the next stage of the definition
             */
            WithCreate withBgp();
        }

        /** Stage of definition allowing to add authorization for the connection. */
        interface WithAuthorization {
            /**
             * Specify authorization key. This is required in case of Express Route connection if Express Route circuit
             * and virtual network gateway reside in different subscriptions.
             *
             * @param authorizationKey authorization key to use
             * @return the next stage of the definition
             */
            WithCreate withAuthorization(String authorizationKey);
        }

        /**
         * The stage of a virtual network gateway connection definition with sufficient inputs to create a new
         * connection in the cloud, but exposing additional optional settings to specify.
         */
        interface WithCreate
            extends Creatable<VirtualNetworkGatewayConnection>,
                Resource.DefinitionWithTags<WithCreate>,
                WithBgp,
                WithAuthorization {
        }
    }

    /** Grouping of virtual network gateway connection update stages. */
    interface Update
        extends Appliable<VirtualNetworkGatewayConnection>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithBgp,
            UpdateStages.WithSharedKey,
            UpdateStages.WithAuthorization {
    }

    /** Grouping of virtual network gateway connection update stages. */
    interface UpdateStages {
        /** Stage of virtual network gateway connection update allowing to enable or disable BGP for the connection. */
        interface WithBgp {
            /**
             * Enable BGP for the connection.
             *
             * @return the next stage of the update
             */
            Update withBgp();

            /**
             * Disable BGP for the connection.
             *
             * @return the next stage of the update
             */
            Update withoutBgp();
        }

        /** Stage of virtual network gateway connection update allowing to specify shared key for the connection. */
        interface WithSharedKey {
            /**
             * Specify shared key.
             *
             * @param sharedKey shared key
             * @return the next stage of the update
             */
            Update withSharedKey(String sharedKey);
        }

        /** Stage of virtual network gateway connection update allowing to add authorization for the connection. */
        interface WithAuthorization {
            /**
             * Specify authorization key. This is required in case of Express Route connection if Express Route circuit
             * and virtual network gateway reside in different subscriptions.
             *
             * @param authorizationKey authorization key to use
             * @return the next stage of the update
             */
            Update withAuthorization(String authorizationKey);
        }
    }
}
