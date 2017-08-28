/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkPeeringInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An client-side representation of a network peering.
 */
@Fluent()
@Beta(SinceVersion.V1_3_0)
public interface NetworkPeering extends
    IndependentChild<NetworkManager>,
    HasInner<VirtualNetworkPeeringInner>,
    Refreshable<NetworkPeering>,
    Updatable<NetworkPeering.Update> {

    /**
     * Possible gateway use scenarios
     */
    enum GatewayUse {
        /**
         * The remote network is allowed to use this network's gateway (but not necessarily using it currently).
         */
        BY_REMOTE_NETWORK,

        /**
         * This network is configured to use the remote network's gateway.
         */
        ON_REMOTE_NETWORK,

        /**
         * No gateway use is configured.
         */
        NONE
    }

    /**
     * @return the local virtual network's ID
     */
    String networkId();

    /**
     * @return the associated remote virtual network's ID
     */
    String remoteNetworkId();

    /**
     * @return the remote network if it is in the same subscription, otherwise null.
     */
    Network getRemoteNetwork();

    /**
     * @return the associated peering on the remote network if it is in the same subscription, otherwise null.
     */
    NetworkPeering getRemotePeering();

    /**
     * @return the state of the peering between the two networks
     */
    VirtualNetworkPeeringState state();

    /**
     * @return the type of gateway use enabled for this network
     */
    GatewayUse gatewayUse();

    /**
     * @return true if traffic forwarding from the remote network is allowed into this network
     */
    boolean isTrafficForwardingFromRemoteNetworkAllowed();

    /**
     * @return true if virtual machines on this network's address spaces are accessible from the remote network
     */
    boolean isAccessFromRemoteNetworkAllowed();

    /**
     * Grouping of all the network peering definition stages.
     */
    interface DefinitionStages {

        /**
         * The first stage of a network peering definition.
         */
        interface Blank extends WithRemoteNetwork {
        }

        /**
         * The stage of a network peering definition allowing to specify the remote virtual network.
         */
        interface WithRemoteNetwork {
            /**
             * Specifies the remote network to peer with.
             * <p>
             * The remote network will have the matching peering associated with it automatically.
             * @param resourceId the resource ID of an existing network
             * @return the next stage of the definition
             */
            WithCreate withRemoteNetwork(String resourceId);

            /**
             * Specifies the remote network to peer with.
             * <p>
             * The remote network will have the matching peering associated with it automatically.
             * @param network an existing network
             * @return the next stage of the definition
             */
            WithCreate withRemoteNetwork(Network network);
        }

        /**
         * The stage of a network peering definition allowing to control traffic forwarding from or to the remote network.
         */
        interface WithTrafficForwarding {
            /**
             * Allows traffic forwarded from the remote network.
             * @return the next stage of the definition
             */
            WithCreate withTrafficForwardingFromRemoteNetwork();

            /**
             * Allows traffic forwarding from this network to the remote network.
             * <p>
             * This setting will only work here if the remote network is in the same subscription. Otherwise, it will be ignored and you need to change
             * the corresponding traffic forwarding setting on the remote network's matching peering explicitly.
             */
            WithCreate withTrafficForwardingToRemoteNetwork();
        }

        /**
         * The stage of a network peering definition allowing to control the gateway use by or on the remote network.
         */
        interface WithGatewayUse {
            /**
             * Allows the remote network to use this network's gateway (a.k.a. gateway transit), but does not start the use of the gateway by the remote network.
             * <p>
             * If this network is currently configured to use the remote network's gateway, that use will be automatically disabled, as these two settings cannot be used together.
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseByRemoteNetworkAllowed();

            /**
             * Allows and starts the use of this network's gateway by the remote network (a.k.a. gateway transit).
             * <p>
             * If the remote network is not in the same subscription as this network, then gateway use by the remote gateway will be allowed on this network, but not started.
             * The matching peering on the remote network must be modified explicitly to start it.
             * <p>
             * If this network is currently configured to use the remote network's gateway, that use will be automatically disabled, as these two settings cannot be used together.
             * <p>
             * Before gateway use by a remote network can be started, a working gateway must already be in place within this network.
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseByRemoteNetworkStarted();

            /**
             * Starts the use of the remote network's gateway.
             * <p>If the remote network is in the same subscription, remote gateway use by this network (a.k.a. gateway transit) will also be automatically allowed on the remote network's side.
             * Otherwise, this network will only be configured to use the remote gateway, but the matching peering on the remote network must still be additionally modified
             * explicitly to allow gateway use by this network.
             * <p>
             * If this network is currently configured to allow the remote network to use its gateway, that use will be automatically disabled, as these two settings cannot be used together.
             * <p>
             * Before gateway use on a remote network can be started, a working gateway must already be in place within the remote network.
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseOnRemoteNetworkStarted();

            /**
             * Disables any gateway use by this network and the remote one.
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseDisabled();
        }

        /**
         * The stage of a network peering definition allowing to control access from and to the remote network.
         */
        interface WithAccess {
            /**
             * Disallows access to this network's address space from the remote network.
             * @return the next stage of the definition
             */
            WithCreate withoutAccessFromRemoteNetwork();

            /**
             * Disallows access to the remote network's address space from this network.
             * <p>
             * This setting will only work here if the remote network is in the same subscription. Otherwise, it will be ignored and you need to change
             * the corresponding access setting on the remote network's matching peering explicitly.
             * @return the next stage of the definition
             */
            WithCreate withoutAccessToRemoteNetwork();
        }

        /**
         * The stage of a network peering definition with sufficient inputs to create a new
         * network peering in the cloud, but exposing additional optional settings to
         * specify.
         */
        interface WithCreate extends
            Creatable<NetworkPeering>,
            WithGatewayUse,
            WithTrafficForwarding,
            WithAccess {
        }
    }

    /**
     * The entirety of the network peering definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithCreate,
        DefinitionStages.WithRemoteNetwork {
    }

    /**
     * The template for a load balancer update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
        Appliable<NetworkPeering> {
    }

    /**
     * Grouping of all the network peering update stages.
     */
    interface UpdateStages {
    }
}
