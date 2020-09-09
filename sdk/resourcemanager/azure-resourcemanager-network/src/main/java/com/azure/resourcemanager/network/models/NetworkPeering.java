// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChild;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;
import reactor.core.publisher.Mono;

/** An client-side representation of a network peering. */
@Fluent()
public interface NetworkPeering
    extends IndependentChild<NetworkManager>,
        HasInner<VirtualNetworkPeeringInner>,
        Refreshable<NetworkPeering>,
        Updatable<NetworkPeering.Update> {

    /** @return the local virtual network's ID */
    String networkId();

    /** @return the reference of the remote virtual network address space */
    List<String> remoteAddressSpaces();

    /**
     * @return true if the peering enables IP addresses within the peered networks to be accessible from both networks,
     *     otherwise false
     *     <p>(Note this method makes a separate call to Azure.)
     */
    boolean checkAccessBetweenNetworks();

    /** @return the associated remote virtual network's ID */
    String remoteNetworkId();

    /** @return the remote network if it is in the same subscription, otherwise null. */
    Network getRemoteNetwork();

    /**
     * Gets the remote network associated with this peering asynchronously.
     *
     * @return a representation of the future computation of this call
     */
    Mono<Network> getRemoteNetworkAsync();

    /**
     * @return the associated matching peering on the remote network if it is in the same subscription, otherwise this
     *     future computation will evaluate to null.
     */
    NetworkPeering getRemotePeering();

    /**
     * Gets the associated matching peering on the remote network if it is in the same subscription.
     *
     * @return a representation of the future computation of this call.
     */
    Mono<NetworkPeering> getRemotePeeringAsync();

    /** @return the state of the peering between the two networks */
    VirtualNetworkPeeringState state();

    /** @return the type of gateway use enabled for this network */
    NetworkPeeringGatewayUse gatewayUse();

    /** @return true if traffic forwarding from the remote network is allowed into this network */
    boolean isTrafficForwardingFromRemoteNetworkAllowed();

    /** @return true if the peered networks are in the same subscription, otherwise false */
    boolean isSameSubscription();

    /** Grouping of all the network peering definition stages. */
    interface DefinitionStages {

        /** The first stage of a network peering definition. */
        interface Blank extends WithRemoteNetwork {
        }

        /** The stage of a network peering definition allowing to specify the remote virtual network. */
        interface WithRemoteNetwork {
            // The remote network can only be specified at the time of the peering creation, not update

            /**
             * Specifies the remote network to peer with.
             *
             * <p>The remote network will have the matching peering associated with it automatically.
             *
             * @param resourceId the resource ID of an existing network
             * @return the next stage of the definition
             */
            WithCreate withRemoteNetwork(String resourceId);

            /**
             * Specifies the remote network to peer with.
             *
             * <p>The remote network will have the matching peering associated with it automatically.
             *
             * @param network an existing network
             * @return the next stage of the definition
             */
            WithCreate withRemoteNetwork(Network network);
        }

        /**
         * The stage of a network peering definition allowing to control traffic forwarding from or to the remote
         * network.
         */
        interface WithTrafficForwarding {
            /**
             * Allows traffic forwarded from the remote network.
             *
             * @return the next stage of the definition
             */
            WithCreate withTrafficForwardingFromRemoteNetwork();

            /**
             * Allows traffic forwarding from this network to the remote network.
             *
             * <p>This setting will have effect only if the remote network is in the same subscription. Otherwise, it
             * will be ignored and you need to change the corresponding traffic forwarding setting on the remote
             * network's matching peering explicitly.
             *
             * @return the next stage of the definition
             */
            WithCreate withTrafficForwardingToRemoteNetwork();

            /**
             * Allows traffic forwarding both from either peered network into the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding traffic forwarding
             * setting on the remote network's matching peering explicitly.
             *
             * @return the next stage of the definition
             */
            WithCreate withTrafficForwardingBetweenBothNetworks();
        }

        /**
         * The stage of a network peering definition allowing to control the gateway use by or on the remote network.
         */
        interface WithGatewayUse {
            /**
             * Allows the remote network to use this network's gateway (a.k.a. gateway transit), but does not start the
             * use of the gateway by the remote network.
             *
             * <p>If this network is currently configured to use the remote network's gateway, that use will be
             * automatically disabled, as these two settings cannot be used together.
             *
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseByRemoteNetworkAllowed();

            /**
             * Allows and starts the use of this network's gateway by the remote network (a.k.a. gateway transit).
             *
             * <p>If the remote network is not in the same subscription as this network, then gateway use by the remote
             * gateway will only be allowed on this network, but not started. The matching peering on the remote network
             * must be modified explicitly to start it.
             *
             * <p>If this network is currently configured to use the remote network's gateway, that use will be
             * automatically disabled, as these two settings cannot be used together.
             *
             * <p>Before gateway use by a remote network can be started, a working gateway must already be in place
             * within this network.
             *
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseByRemoteNetworkStarted();

            /**
             * Starts the use of the remote network's gateway.
             *
             * <p>If the remote network is in the same subscription, remote gateway use by this network (a.k.a. gateway
             * transit) will also be automatically allowed on the remote network's side. Otherwise, this network will
             * only be configured to use the remote gateway, but the matching peering on the remote network must still
             * be additionally modified explicitly to allow gateway use by this network.
             *
             * <p>If this network is currently configured to allow the remote network to use its gateway, that use will
             * be automatically disabled, as these two settings cannot be used together.
             *
             * <p>Before gateway use on a remote network can be started, a working gateway must already be in place
             * within the remote network.
             *
             * @return the next stage of the definition
             */
            WithCreate withGatewayUseOnRemoteNetworkStarted();

            /**
             * Disables any gateway use by this network and the remote one.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutAnyGatewayUse();
        }

        /** The stage of a network peering definition allowing to control access from and to the remote network. */
        interface WithAccess {
            /**
             * Disallows access to either peered network from the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding access setting on
             * the remote network's matching peering explicitly.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutAccessFromEitherNetwork();
        }

        /**
         * The stage of a network peering definition with sufficient inputs to create a new network peering in the
         * cloud, but exposing additional optional settings to specify.
         */
        interface WithCreate extends Creatable<NetworkPeering>, WithGatewayUse, WithTrafficForwarding, WithAccess {
        }
    }

    /** The entirety of the network peering definition. */
    interface Definition
        extends DefinitionStages.Blank, DefinitionStages.WithCreate, DefinitionStages.WithRemoteNetwork {
    }

    /** The template for a network peering update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<NetworkPeering>,
            UpdateStages.WithTrafficForwarding,
            UpdateStages.WithAccess,
            UpdateStages.WithGatewayUse {
    }

    /** Grouping of all the network peering update stages. */
    interface UpdateStages {

        /** The stage of a network peering update allowing to control the gateway use by or on the remote network. */
        interface WithGatewayUse {
            /**
             * Allows the remote network to use this network's gateway (a.k.a. gateway transit), but does not start the
             * use of the gateway by the remote network.
             *
             * <p>If this network is currently configured to use the remote network's gateway, that use will be
             * automatically disabled, as these two settings cannot be used together.
             *
             * @return the next stage of the update
             */
            Update withGatewayUseByRemoteNetworkAllowed();

            /**
             * Allows and starts the use of this network's gateway by the remote network (a.k.a. gateway transit).
             *
             * <p>If the remote network is not in the same subscription as this network, then gateway use by the remote
             * gateway will only be allowed on this network, but not started. The matching peering on the remote network
             * must be modified explicitly to start it.
             *
             * <p>If this network is currently configured to use the remote network's gateway, that use will be
             * automatically disabled, as these two settings cannot be used together.
             *
             * <p>Before gateway use by a remote network can be started, a working gateway must already be in place
             * within this network.
             *
             * @return the next stage of the update
             */
            Update withGatewayUseByRemoteNetworkStarted();

            /**
             * Starts the use of the remote network's gateway.
             *
             * <p>If the remote network is in the same subscription, remote gateway use by this network (a.k.a. gateway
             * transit) will also be automatically allowed on the remote network's side. Otherwise, this network will
             * only be configured to use the remote gateway, but the matching peering on the remote network must still
             * be additionally modified explicitly to allow gateway use by this network.
             *
             * <p>If this network is currently configured to allow the remote network to use its gateway, that use will
             * be automatically disabled, as these two settings cannot be used together.
             *
             * <p>Before gateway use on a remote network can be started, a working gateway must already be in place
             * within the remote network.
             *
             * @return the next stage of the update
             */
            Update withGatewayUseOnRemoteNetworkStarted();

            /**
             * Stops this network's use of the remote network's gateway.
             *
             * @return the next stage of the definition.
             */
            Update withoutGatewayUseOnRemoteNetwork();

            /**
             * Disables any gateway use by this network and the remote one.
             *
             * <p>This will have effect on the remote network only if the remote network is in the same subscription as
             * this network. Otherwise, only this network's use of the remote network's gateway will be stopped, but the
             * use of this network's gateway by the remote network will only be disallowed. You will have to update the
             * remote network's peering explicitly to properly stop its use of this network's gateway.
             *
             * @return the next stage of the update
             */
            Update withoutAnyGatewayUse();

            /**
             * Stops and disallows the use of this network's gateway by the remote network.
             *
             * <p>If the remote network is not in the same subscription, then the use of that network's gateway by this
             * network will be stopped but not disallowed by the remote network. The matching peering on the remote
             * network must still be explicitly updated to also disallow such use.
             *
             * @return the next stage of the update
             */
            Update withoutGatewayUseByRemoteNetwork();
        }

        /**
         * The stage of a network peering update allowing to control traffic forwarding from or to the remote network.
         */
        interface WithTrafficForwarding {
            /**
             * Allows traffic forwarding from the remote network.
             *
             * @return the next stage of the update
             */
            Update withTrafficForwardingFromRemoteNetwork();

            /**
             * Prevents traffic forwarding from the remote network.
             *
             * @return the next stage of the update
             */
            Update withoutTrafficForwardingFromRemoteNetwork();

            /**
             * Allows traffic forwarding from this network to the remote network.
             *
             * <p>This setting will only work here if the remote network is in the same subscription. Otherwise, it will
             * be ignored and you need to change the corresponding traffic forwarding setting on the remote network's
             * matching peering explicitly.
             *
             * @return the next stage of the update
             */
            Update withTrafficForwardingToRemoteNetwork();

            /**
             * Disables traffic forwarding to the remote network.
             *
             * @return the next stage of the update
             */
            Update withoutTrafficForwardingToRemoteNetwork();

            /**
             * Allows traffic forwarding both from either peered network to the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding traffic forwarding
             * setting on the remote network's matching peering explicitly.
             *
             * @return the next stage of the update
             */
            Update withTrafficForwardingBetweenBothNetworks();

            /**
             * Disables traffic forwarding from either peered network to the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding traffic forwarding
             * setting on the remote network's matching peering explicitly.
             *
             * @return the next stage of the update
             */
            Update withoutTrafficForwardingFromEitherNetwork();
        }

        /** The stage of a network peering update allowing to control access from and to the remote network. */
        interface WithAccess {
            /**
             * Enables access to either peered virtual network from the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding access setting on
             * the remote network's matching peering explicitly.
             *
             * @return the next stage of the update
             */
            Update withAccessBetweenBothNetworks();

            /**
             * Disallows access to either peered network from the other.
             *
             * <p>This setting will have effect on the remote network only if the remote network is in the same
             * subscription. Otherwise, it will be ignored and you need to change the corresponding access setting on
             * the remote network's matching peering explicitly.
             *
             * @return the next stage of the update
             */
            Update withoutAccessFromEitherNetwork();
        }
    }
}
