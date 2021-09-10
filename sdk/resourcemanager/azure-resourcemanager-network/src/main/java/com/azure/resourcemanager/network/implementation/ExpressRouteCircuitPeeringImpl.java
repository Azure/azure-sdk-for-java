// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCircuitPeeringsClient;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.network.fluent.models.Ipv6ExpressRouteCircuitPeeringConfigInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringState;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringType;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;

class ExpressRouteCircuitPeeringImpl<
        ParentModelT,
        ParentInnerT,
        ParentT extends GroupableResource<NetworkManager, ParentInnerT> & Refreshable<ParentModelT>>
    extends CreatableUpdatableImpl<
        ExpressRouteCircuitPeering,
        ExpressRouteCircuitPeeringInner,
        ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT>>
    implements ExpressRouteCircuitPeering, ExpressRouteCircuitPeering.Definition, ExpressRouteCircuitPeering.Update {
    private final ExpressRouteCircuitPeeringsClient client;
    private final ParentT parent;
    private ExpressRouteCircuitStatsImpl stats;

    ExpressRouteCircuitPeeringImpl(
        ParentT parent,
        ExpressRouteCircuitPeeringInner innerObject,
        ExpressRouteCircuitPeeringsClient client,
        ExpressRoutePeeringType type) {
        super(type.toString(), innerObject);
        this.client = client;
        this.parent = parent;
        this.stats = new ExpressRouteCircuitStatsImpl(innerObject.stats());
        innerModel().withPeeringType(type);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT> withAdvertisedPublicPrefixes(
        String publicPrefix) {
        ensureMicrosoftPeeringConfig().withAdvertisedPublicPrefixes(Arrays.asList(publicPrefix));
        return this;
    }

    private ExpressRouteCircuitPeeringConfig ensureMicrosoftPeeringConfig() {
        if (innerModel().microsoftPeeringConfig() == null) {
            innerModel().withMicrosoftPeeringConfig(new ExpressRouteCircuitPeeringConfig());
        }
        return innerModel().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT> withPrimaryPeerAddressPrefix(
        String addressPrefix) {
        innerModel().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT> withSecondaryPeerAddressPrefix(
        String addressPrefix) {
        innerModel().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT> withVlanId(int vlanId) {
        innerModel().withVlanId(vlanId);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ParentModelT, ParentInnerT, ParentT> withPeerAsn(long peerASN) {
        innerModel().withPeerAsn(peerASN);
        return this;
    }

    @Override
    protected Mono<ExpressRouteCircuitPeeringInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public Mono<ExpressRouteCircuitPeering> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), innerModel())
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
        return innerModel().id();
    }

    @Override
    public ExpressRoutePeeringType peeringType() {
        return innerModel().peeringType();
    }

    @Override
    public ExpressRoutePeeringState state() {
        return innerModel().state();
    }

    @Override
    public int azureAsn() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().azureAsn());
    }

    @Override
    public long peerAsn() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().peerAsn());
    }

    @Override
    public String primaryPeerAddressPrefix() {
        return innerModel().primaryPeerAddressPrefix();
    }

    @Override
    public String secondaryPeerAddressPrefix() {
        return innerModel().secondaryPeerAddressPrefix();
    }

    @Override
    public String primaryAzurePort() {
        return innerModel().primaryAzurePort();
    }

    @Override
    public String secondaryAzurePort() {
        return innerModel().secondaryAzurePort();
    }

    @Override
    public String sharedKey() {
        return innerModel().sharedKey();
    }

    @Override
    public int vlanId() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().vlanId());
    }

    @Override
    public ExpressRouteCircuitPeeringConfig microsoftPeeringConfig() {
        return innerModel().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitStatsImpl stats() {
        return stats;
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public String lastModifiedBy() {
        return innerModel().lastModifiedBy();
    }

    @Override
    public Ipv6ExpressRouteCircuitPeeringConfigInner ipv6PeeringConfig() {
        return innerModel().ipv6PeeringConfig();
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
