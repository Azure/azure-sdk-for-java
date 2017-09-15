/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkGatewayConnectionInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.Collection;
import java.util.List;

/**
 * Client-side representation of Virtual Network Gateway Connection object, associated with Virtual Network Gateway.
 */
@Fluent
@Beta
public interface VirtualNetworkGatewayConnection extends
        IndependentChildResource<NetworkManager, VirtualNetworkGatewayConnectionInner>,
        Refreshable<VirtualNetworkGatewayConnection>,
        Updatable<VirtualNetworkGatewayConnection>,
        HasParent<VirtualNetworkGateway> {

    /**
     * Get the authorizationKey value.
     *
     * @return the authorizationKey value
     */
    String authorizationKey();

    /**
     * @return the reference to virtual network gateway resource
     */
    String virtualNetworkGateway1Id();

    /**
     * @return the reference to virtual network gateway resource.
     */
    String virtualNetworkGateway2Id();

    /**
     * @return the reference to local network gateway resource
     */
    String localNetworkGateway2Id();

    /**
     * Get the gateway connection type. Possible values are:
     * 'Ipsec','Vnet2Vnet','ExpressRoute', and 'VPNClient.
     *
     * @return the connectionType value
     */
    VirtualNetworkGatewayConnectionType connectionType();

    /**
     * @return the routing weight
     */
    int routingWeight();

    /**
     * @return the IPSec shared key
     */
    String sharedKey();

    /**
     * Get the Virtual Network Gateway connection status. Possible values are
     * 'Unknown', 'Connecting', 'Connected' and 'NotConnected'. Possible values
     * include: 'Unknown', 'Connecting', 'Connected', 'NotConnected'.
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

    /**
     * @return the egress bytes transferred in this connection
     */
    long egressBytesTransferred();

    /**
     * @return the egress bytes transferred in this connection.
     */
    long ingressBytesTransferred();

    /**
     * @return the reference to peerings resource
     */
    String peerId();

    /**
     * @return the enableBgp flag
     */
    Boolean isBgpEnabled();

    /**
     * @return if policy-based traffic selectors enabled
     */
    boolean usePolicyBasedTrafficSelectors();

    /**
     * @return the IPSec Policies to be considered by this connection
     */
    List<IpsecPolicy> ipsecPolicies();

    /**
     * @return the provisioning state of the VirtualNetworkGatewayConnection resource
     */
    String provisioningState();

    /**
     * The entirety of the virtual network gateway connection definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithConnectionType,
            DefinitionStages.WithLocalNetworkGateway,
            DefinitionStages.WithSecondVirtualNetworkGateway,
            DefinitionStages.WithExpressRoute,
            DefinitionStages.WithSharedKey,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual network gateway connection definition stages.
     */
    interface DefinitionStages {
        interface Blank extends WithConnectionType {
        }

        interface WithConnectionType {
            WithLocalNetworkGateway withSiteToSite();

            WithSecondVirtualNetworkGateway withVNetToVNet();

            WithExpressRoute withExpressRoute();
        }

        interface WithLocalNetworkGateway {
            WithSharedKey withLocalNetworkGateway(LocalNetworkGateway localNetworkGateway);
        }

        interface WithSecondVirtualNetworkGateway {
            WithSharedKey withSecondVirtualNetworkGateway(VirtualNetworkGateway virtualNetworkGateway2);
        }

        interface WithExpressRoute {
        }

        interface WithSharedKey {
            WithCreate withSharedKey(String sharedKey);
        }

        interface WithBgp {
            WithCreate withBgp();
        }

        interface WithCreate extends
                Creatable<VirtualNetworkGatewayConnection>,
                Resource.DefinitionWithTags<WithCreate>,
                WithBgp {
        }
    }

    /**
     * Grouping of virtual network gateway connection update stages.
     */
    interface Update extends
            UpdateStages.WithBgp {
    }

    /**
     * Grouping of virtual network gateway connection update stages.
     */
    interface UpdateStages {

        interface WithBgp {
            Update withBgp();

            Update withoutBgp();
        }
    }
}
