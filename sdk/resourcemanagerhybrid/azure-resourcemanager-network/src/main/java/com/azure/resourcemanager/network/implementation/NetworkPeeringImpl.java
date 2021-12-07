// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkPeeringInner;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkPeering;
import com.azure.resourcemanager.network.models.NetworkPeering.DefinitionStages.WithCreate;
import com.azure.resourcemanager.network.models.NetworkPeeringGatewayUse;
import com.azure.resourcemanager.network.models.VirtualNetworkPeeringState;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/** Implementation for network peering. */
class NetworkPeeringImpl
    extends IndependentChildImpl<
        NetworkPeering, Network, VirtualNetworkPeeringInner, NetworkPeeringImpl, NetworkManager>
    implements NetworkPeering, NetworkPeering.Definition, NetworkPeering.Update {

    private NetworkImpl parent;
    private Network remoteNetwork;
    private Boolean remoteAccess; // Controls the allowAccess setting on the remote peering (null means no change)
    private Boolean
        remoteForwarding; // Controls the trafficForwarding setting on the remote peering (null means no change)
    private Boolean
        startGatewayUseByRemoteNetwork; // Controls the UseGateway setting on the remote network (null means no change)
    private Boolean
        allowGatewayUseOnRemoteNetwork; // Controls the AllowGatewayTransit setting on the remote network (null means no
    // change)

    NetworkPeeringImpl(VirtualNetworkPeeringInner inner, NetworkImpl parent) {
        super(inner.name(), inner, parent.manager());
        this.parent = parent;
        this.remoteAccess = null;
        this.remoteForwarding = null;
    }

    // Getters

    @Override
    public boolean isSameSubscription() {
        if (this.remoteNetworkId() == null) {
            return false;
        }
        String localSubscriptionId = ResourceUtils.subscriptionFromResourceId(this.id());
        String remoteSubscriptionId = ResourceUtils.subscriptionFromResourceId(this.remoteNetworkId());
        return localSubscriptionId.equalsIgnoreCase(remoteSubscriptionId);
    }

    @Override
    public VirtualNetworkPeeringState state() {
        return this.innerModel().peeringState();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String networkId() {
        return this.parent.id();
    }

    @Override
    public List<String> remoteAddressSpaces() {
        if (innerModel().remoteAddressSpace() == null || innerModel().remoteAddressSpace().addressPrefixes() == null) {
            return null;
        }
        return Collections.unmodifiableList(innerModel().remoteAddressSpace().addressPrefixes());
    }

    @Override
    public String remoteNetworkId() {
        return (this.innerModel().remoteVirtualNetwork() != null)
            ? this.innerModel().remoteVirtualNetwork().id()
            : null;
    }

    @Override
    public boolean isTrafficForwardingFromRemoteNetworkAllowed() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().allowForwardedTraffic());
    }

    private boolean isAccessFromRemoteNetworkAllowed() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().allowVirtualNetworkAccess());
    }

    // Fluent setters

    private NetworkPeeringImpl withoutAccessFromRemoteNetwork() {
        this.innerModel().withAllowVirtualNetworkAccess(false);
        return this;
    }

    private NetworkPeeringImpl withAccessFromRemoteNetwork() {
        this.innerModel().withAllowVirtualNetworkAccess(true);
        return this;
    }

    @Override
    public NetworkPeeringImpl withAccessBetweenBothNetworks() {
        return this.withAccessFromRemoteNetwork().withAccessToRemoteNetwork();
    }

    @Override
    public NetworkPeeringImpl withoutAccessFromEitherNetwork() {
        return this.withoutAccessFromRemoteNetwork().withoutAccessToRemoteNetwork();
    }

    @Override
    public NetworkPeeringImpl withRemoteNetwork(String resourceId) {
        SubResource networkRef = new SubResource().withId(resourceId);
        this.innerModel().withRemoteVirtualNetwork(networkRef);
        return this;
    }

    @Override
    public NetworkPeeringImpl withRemoteNetwork(Network network) {
        if (network != null) {
            this.remoteNetwork = network;
            return this.withRemoteNetwork(network.id());
        }
        return this;
    }

    @Override
    public NetworkPeeringImpl withTrafficForwardingFromRemoteNetwork() {
        this.innerModel().withAllowForwardedTraffic(true);
        return this;
    }

    @Override
    public NetworkPeeringImpl withoutTrafficForwardingFromRemoteNetwork() {
        this.innerModel().withAllowForwardedTraffic(false);
        return this;
    }

    @Override
    public NetworkPeeringImpl withoutTrafficForwardingFromEitherNetwork() {
        return this.withoutTrafficForwardingFromRemoteNetwork().withoutTrafficForwardingToRemoteNetwork();
    }

    @Override
    public NetworkPeeringImpl withTrafficForwardingToRemoteNetwork() {
        this.remoteForwarding = true;
        return this;
    }

    @Override
    public NetworkPeeringImpl withoutTrafficForwardingToRemoteNetwork() {
        this.remoteForwarding = false;
        return this;
    }

    @Override
    public NetworkPeeringImpl withTrafficForwardingBetweenBothNetworks() {
        return this.withTrafficForwardingFromRemoteNetwork().withTrafficForwardingToRemoteNetwork();
    }

    private NetworkPeeringImpl withoutAccessToRemoteNetwork() {
        this.remoteAccess = false;
        return this;
    }

    private NetworkPeeringImpl withAccessToRemoteNetwork() {
        this.remoteAccess = true;
        return this;
    }

    @Override
    public NetworkPeeringImpl withGatewayUseByRemoteNetworkAllowed() {
        this.innerModel().withAllowGatewayTransit(true).withUseRemoteGateways(false);
        this.startGatewayUseByRemoteNetwork = null;
        this.allowGatewayUseOnRemoteNetwork = false;
        return this;
    }

    @Override
    public NetworkPeeringImpl withGatewayUseByRemoteNetworkStarted() {
        this.startGatewayUseByRemoteNetwork = true;
        this.allowGatewayUseOnRemoteNetwork = false;
        return this.withGatewayUseByRemoteNetworkAllowed();
    }

    @Override
    public NetworkPeeringImpl withGatewayUseOnRemoteNetworkStarted() {
        this.innerModel().withAllowGatewayTransit(false).withUseRemoteGateways(true);
        this.startGatewayUseByRemoteNetwork = false;
        this.allowGatewayUseOnRemoteNetwork = true;
        return this;
    }

    @Override
    public NetworkPeeringImpl withoutAnyGatewayUse() {
        this.innerModel().withAllowGatewayTransit(false);
        return this.withoutGatewayUseOnRemoteNetwork().withoutGatewayUseByRemoteNetwork();
    }

    @Override
    public NetworkPeeringImpl withoutGatewayUseByRemoteNetwork() {
        this.startGatewayUseByRemoteNetwork = false;
        this.allowGatewayUseOnRemoteNetwork = false;
        return this;
    }

    @Override
    public NetworkPeeringImpl withoutGatewayUseOnRemoteNetwork() {
        this.innerModel().withUseRemoteGateways(false);
        return this;
    }

    // Actions

    @Override
    public boolean checkAccessBetweenNetworks() {
        if (!ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().allowVirtualNetworkAccess())) {
            // If network access is disabled on this peering, then it's disabled for both networks, regardless of what
            // the remote peering says
            return false;
        }

        // Check the access setting on the remote peering
        NetworkPeering remotePeering = this.getRemotePeering();
        if (remotePeering == null) {
            return false;
        } else {
            // Access is enabled on local peering, so up to the remote peering to determine whether it's enabled or
            // disabled overall
            return ResourceManagerUtils.toPrimitiveBoolean(remotePeering.innerModel().allowVirtualNetworkAccess());
        }
    }

    @Override
    protected Mono<NetworkPeering> createChildResourceAsync() {
        final NetworkPeeringImpl localPeering = this;
        final String networkName = ResourceUtils.nameFromResourceId(this.networkId());
        return this
            .manager()
            .serviceClient()
            .getVirtualNetworkPeerings()
            .createOrUpdateAsync(this.parent.resourceGroupName(), networkName, this.name(), this.innerModel())
            // After successful creation, update the inner
            .doOnNext(
                inner -> {
                    setInner(inner);
                })

            // Then get the remote network to update it if needed and in the same subscription
            .flatMap(
                inner -> {
                    SubResource networkRef = inner.remoteVirtualNetwork();
                    if (localPeering.isSameSubscription()) {
                        // Update the remote network only if it is in the same subscription
                        return localPeering.manager().networks().getByIdAsync(networkRef.id());
                    } else {
                        // Otherwise, skip this
                        return Mono.empty();
                    }
                })

            // Then update the existing remote network if needed, flatMap will skip when input is empty
            .flatMap(
                remoteNetwork -> {
                    // Check if any peering is already pointing at this network
                    return remoteNetwork
                        .peerings()
                        .listAsync()
                        .filter(
                            remotePeering ->
                                (remotePeering != null
                                    && remotePeering.remoteNetworkId() != null
                                    && remotePeering.remoteNetworkId().equalsIgnoreCase(localPeering.parent.id())))
                        .collectList()
                        .flatMap(
                            remotePeerings -> {
                                if (remotePeerings.isEmpty()) {
                                    return Mono.empty();
                                } else {
                                    return Mono.justOrEmpty(remotePeerings.get(remotePeerings.size() - 1));
                                }
                            })

                        // Depending on the existence of a matching remote peering, create one or update existing
                        .flatMap(
                            remotePeering -> {
                                // Matching peering exists, so update as needed
                                Update remotePeeringUpdate = remotePeering.update();
                                boolean isUpdateNeeded = false;

                                // Update traffic forwarding on the remote peering if needed
                                if (localPeering.remoteForwarding != null) {
                                    if (localPeering.remoteForwarding.booleanValue()
                                        && !remotePeering.isTrafficForwardingFromRemoteNetworkAllowed()) {
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate =
                                            remotePeeringUpdate.withTrafficForwardingFromRemoteNetwork();
                                    } else if (!localPeering.remoteForwarding.booleanValue()
                                        && remotePeering.isTrafficForwardingFromRemoteNetworkAllowed()) {
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate =
                                            remotePeeringUpdate.withoutTrafficForwardingFromRemoteNetwork();
                                    }
                                }

                                // Update network access on the remote peering if needed
                                if (localPeering.remoteAccess != null) {
                                    if (localPeering.remoteAccess.booleanValue()
                                        && !((NetworkPeeringImpl) remotePeering).isAccessFromRemoteNetworkAllowed()) {
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate =
                                            ((NetworkPeeringImpl) remotePeeringUpdate).withAccessFromRemoteNetwork();
                                    } else if (!localPeering.remoteAccess.booleanValue()
                                        && ((NetworkPeeringImpl) remotePeering).isAccessFromRemoteNetworkAllowed()) {
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate =
                                            ((NetworkPeeringImpl) remotePeeringUpdate).withoutAccessFromRemoteNetwork();
                                    }
                                }

                                // Update gateway use permission on the remote peering if needed
                                if (localPeering.allowGatewayUseOnRemoteNetwork != null) {
                                    if (localPeering.allowGatewayUseOnRemoteNetwork.booleanValue()
                                        && remotePeering.gatewayUse() != NetworkPeeringGatewayUse.BY_REMOTE_NETWORK) {
                                        // Allow gateway use on remote network
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate.withGatewayUseByRemoteNetworkAllowed();
                                    } else if (!localPeering.allowGatewayUseOnRemoteNetwork.booleanValue()
                                        && remotePeering.gatewayUse() == NetworkPeeringGatewayUse.BY_REMOTE_NETWORK) {
                                        // Disallow gateway use on remote network
                                        isUpdateNeeded = true;
                                        remotePeeringUpdate.withoutGatewayUseByRemoteNetwork();
                                    }
                                }

                                // Update gateway use start on the remote peering if needed
                                if (localPeering.startGatewayUseByRemoteNetwork != null) {
                                    if (localPeering.startGatewayUseByRemoteNetwork.booleanValue()
                                        && remotePeering.gatewayUse() != NetworkPeeringGatewayUse.ON_REMOTE_NETWORK) {
                                        remotePeeringUpdate.withGatewayUseOnRemoteNetworkStarted();
                                        isUpdateNeeded = true;
                                    } else if (!localPeering.startGatewayUseByRemoteNetwork.booleanValue()
                                        && remotePeering.gatewayUse() == NetworkPeeringGatewayUse.ON_REMOTE_NETWORK) {
                                        remotePeeringUpdate.withoutGatewayUseOnRemoteNetwork();
                                        isUpdateNeeded = true;
                                    }
                                }

                                if (isUpdateNeeded) {
                                    localPeering.remoteForwarding = null;
                                    localPeering.remoteAccess = null;
                                    localPeering.startGatewayUseByRemoteNetwork = null;
                                    localPeering.allowGatewayUseOnRemoteNetwork = null;
                                    return remotePeeringUpdate.applyAsync();
                                } else {
                                    return Mono.just((Indexable) localPeering);
                                }
                            })
                        .switchIfEmpty(
                            Mono
                                .defer(
                                    () -> {
                                        // No matching remote peering, so create one on the remote network
                                        String peeringName = this.manager().resourceManager().internalContext()
                                            .randomResourceName("peer", 15);

                                        WithCreate remotePeeringDefinition =
                                            remoteNetwork
                                                .peerings()
                                                .define(peeringName)
                                                .withRemoteNetwork(localPeering.parent.id());

                                        // Process remote network's UseRemoteGateways setting
                                        if (localPeering.startGatewayUseByRemoteNetwork != null) {
                                            if (localPeering.startGatewayUseByRemoteNetwork.booleanValue()) {
                                                // Start gateway use on this network by the remote network
                                                remotePeeringDefinition.withGatewayUseOnRemoteNetworkStarted();
                                            }
                                        }

                                        // Process remote network's AllowGatewayTransit setting
                                        if (localPeering.allowGatewayUseOnRemoteNetwork != null) {
                                            if (localPeering.allowGatewayUseOnRemoteNetwork.booleanValue()) {
                                                // Allow gateway use on remote network
                                                remotePeeringDefinition.withGatewayUseByRemoteNetworkAllowed();
                                            }
                                        }

                                        if (localPeering.remoteAccess != null && !localPeering.remoteAccess) {
                                            ((NetworkPeeringImpl) remotePeeringDefinition)
                                                .withoutAccessFromRemoteNetwork(); // Assumes by default access is on
                                            // for new peerings
                                        }

                                        if (localPeering.remoteForwarding != null
                                            && localPeering.remoteForwarding.booleanValue()) {
                                            remotePeeringDefinition
                                                .withTrafficForwardingFromRemoteNetwork(); // Assumes by default
                                            // forwarding is off for new
                                            // peerings
                                        }

                                        localPeering.remoteAccess = null;
                                        localPeering.remoteForwarding = null;
                                        localPeering.startGatewayUseByRemoteNetwork = null;
                                        localPeering.allowGatewayUseOnRemoteNetwork = null;
                                        return remotePeeringDefinition.createAsync();
                                    }));
                })

            // Then refresh the parent local network, if available
            .flatMap(remotePeering -> (localPeering.parent != null) ? localPeering.parent.refreshAsync() : Mono.empty())

            // Then refresh the remote network, if available and in the same subscription
            .flatMap(
                t -> {
                    if (localPeering.remoteNetwork != null && localPeering.isSameSubscription()) {
                        return localPeering.remoteNetwork.refreshAsync();
                    } else {
                        return Mono.empty();
                    }
                })

            // Then return the created local peering
            .then(Mono.just(localPeering));
    }

    @Override
    protected Mono<VirtualNetworkPeeringInner> getInnerAsync() {
        this.remoteNetwork = null;
        return this
            .manager()
            .serviceClient()
            .getVirtualNetworkPeerings()
            .getAsync(
                this.resourceGroupName(), ResourceUtils.nameFromResourceId(this.networkId()), this.innerModel().name());
    }

    @Override
    public Network getRemoteNetwork() {
        return this.getRemoteNetworkAsync().block();
    }

    @Override
    public Mono<Network> getRemoteNetworkAsync() {
        final NetworkPeeringImpl self = this;
        if (self.remoteNetwork != null) {
            return Mono.just(self.remoteNetwork);
        } else if (this.isSameSubscription()) {
            // Fetch the remote network if within the same subscription
            return this
                .manager()
                .networks()
                .getByIdAsync(this.remoteNetworkId())
                .doOnNext(
                    network -> {
                        self.remoteNetwork = network;
                    });
        } else {
            // Otherwise bail out
            self.remoteNetwork = null;
            return Mono.empty();
        }
    }

    @Override
    public NetworkPeering getRemotePeering() {
        Network network = this.getRemoteNetwork();
        return (network != null) ? network.peerings().getByRemoteNetwork(this.networkId()) : null;
    }

    @Override
    public Mono<NetworkPeering> getRemotePeeringAsync() {
        final NetworkPeeringImpl self = this;
        return this
            .getRemoteNetworkAsync()
            .flatMap(
                remoteNetwork -> {
                    if (remoteNetwork == null) {
                        return Mono.empty();
                    } else {
                        return remoteNetwork.peerings().getByRemoteNetworkAsync(self.networkId());
                    }
                });
    }

    @Override
    public NetworkPeeringGatewayUse gatewayUse() {
        if (ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().allowGatewayTransit())) {
            return NetworkPeeringGatewayUse.BY_REMOTE_NETWORK;
        } else if (ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().useRemoteGateways())) {
            return NetworkPeeringGatewayUse.ON_REMOTE_NETWORK;
        } else {
            return NetworkPeeringGatewayUse.NONE;
        }
    }
}
