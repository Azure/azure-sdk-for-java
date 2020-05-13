// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.implementation;

import com.azure.management.network.ExpressRouteCircuitPeering;
import com.azure.management.network.ExpressRouteCircuitPeeringConfig;
import com.azure.management.network.ExpressRoutePeeringState;
import com.azure.management.network.ExpressRoutePeeringType;
import com.azure.management.network.Ipv6ExpressRouteCircuitPeeringConfig;
import com.azure.management.network.models.ExpressRouteCircuitPeeringInner;
import com.azure.management.network.models.ExpressRouteCircuitPeeringsInner;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Arrays;

class ExpressRouteCircuitPeeringImpl
    <ParentModelT, ParentInnerT,
        ParentT extends GroupableResource<NetworkManager, ParentInnerT> & Refreshable<ParentModelT>>
    extends CreatableUpdatableImpl<
        ExpressRouteCircuitPeering, ExpressRouteCircuitPeeringInner,
        ExpressRouteCircuitPeeringImpl< ParentModelT, ParentInnerT, ParentT>>
    implements ExpressRouteCircuitPeering, ExpressRouteCircuitPeering.Definition, ExpressRouteCircuitPeering.Update {
    private final ExpressRouteCircuitPeeringsInner client;
    private final ParentT parent;
    private ExpressRouteCircuitStatsImpl stats;

    ExpressRouteCircuitPeeringImpl(
        ParentT parent,
        ExpressRouteCircuitPeeringInner innerObject,
        ExpressRouteCircuitPeeringsInner client,
        ExpressRoutePeeringType type) {
        super(type.toString(), innerObject);
        this.client = client;
        this.parent = parent;
        this.stats = new ExpressRouteCircuitStatsImpl(innerObject.stats());
        inner().withPeeringType(type);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withAdvertisedPublicPrefixes(String publicPrefix) {
        ensureMicrosoftPeeringConfig().withAdvertisedPublicPrefixes(Arrays.asList(publicPrefix));
        return this;
    }

    private ExpressRouteCircuitPeeringConfig ensureMicrosoftPeeringConfig() {
        if (inner().microsoftPeeringConfig() == null) {
            inner().withMicrosoftPeeringConfig(new ExpressRouteCircuitPeeringConfig());
        }
        return inner().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withPrimaryPeerAddressPrefix(String addressPrefix) {
        inner().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withSecondaryPeerAddressPrefix(String addressPrefix) {
        inner().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withVlanId(int vlanId) {
        inner().withVlanId(vlanId);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withPeerASN(long peerASN) {
        inner().withPeerASN(peerASN);
        return this;
    }

    @Override
    protected Mono<ExpressRouteCircuitPeeringInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public Mono<ExpressRouteCircuitPeering> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), inner())
            .flatMap(
                innerModel -> {
                    this.setInner(innerModel);
                    stats = new ExpressRouteCircuitStatsImpl(innerModel.stats());
                    return parent.refreshAsync().then(Mono.just(this));
                });
    }

    // Getters

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public ExpressRoutePeeringType peeringType() {
        return inner().peeringType();
    }

    @Override
    public ExpressRoutePeeringState state() {
        return inner().state();
    }

    @Override
    public int azureASN() {
        return Utils.toPrimitiveInt(inner().azureASN());
    }

    @Override
    public long peerASN() {
        return Utils.toPrimitiveLong(inner().peerASN());
    }

    @Override
    public String primaryPeerAddressPrefix() {
        return inner().primaryPeerAddressPrefix();
    }

    @Override
    public String secondaryPeerAddressPrefix() {
        return inner().secondaryPeerAddressPrefix();
    }

    @Override
    public String primaryAzurePort() {
        return inner().primaryAzurePort();
    }

    @Override
    public String secondaryAzurePort() {
        return inner().secondaryAzurePort();
    }

    @Override
    public String sharedKey() {
        return inner().sharedKey();
    }

    @Override
    public int vlanId() {
        return Utils.toPrimitiveInt(inner().vlanId());
    }

    @Override
    public ExpressRouteCircuitPeeringConfig microsoftPeeringConfig() {
        return inner().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitStatsImpl stats() {
        return stats;
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public String lastModifiedBy() {
        return inner().lastModifiedBy();
    }

    @Override
    public Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig() {
        return inner().ipv6PeeringConfig();
    }

    @Override
    public NetworkManager manager() {
        return parent.manager();
    }

    @Override
    public String resourceGroupName() {
        return parent.resourceGroupName();
    }
}
