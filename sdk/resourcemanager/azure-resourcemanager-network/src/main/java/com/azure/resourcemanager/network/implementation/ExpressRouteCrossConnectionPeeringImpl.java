// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCrossConnectionPeeringsClient;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringState;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringType;
import com.azure.resourcemanager.network.models.Ipv6ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Arrays;

class ExpressRouteCrossConnectionPeeringImpl
    extends CreatableUpdatableImpl<
        ExpressRouteCrossConnectionPeering,
        ExpressRouteCrossConnectionPeeringInner,
        ExpressRouteCrossConnectionPeeringImpl>
    implements ExpressRouteCrossConnectionPeering,
        ExpressRouteCrossConnectionPeering.Definition,
        ExpressRouteCrossConnectionPeering.Update {
    private final ExpressRouteCrossConnectionPeeringsClient client;
    private final ExpressRouteCrossConnection parent;

    ExpressRouteCrossConnectionPeeringImpl(
        ExpressRouteCrossConnectionImpl parent,
        ExpressRouteCrossConnectionPeeringInner innerObject,
        ExpressRoutePeeringType type) {
        super(type.toString(), innerObject);
        this.client = parent.manager().inner().getExpressRouteCrossConnectionPeerings();
        this.parent = parent;
        inner().withPeeringType(type);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withAdvertisedPublicPrefixes(String publicPrefix) {
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
    public ExpressRouteCrossConnectionPeeringImpl withPrimaryPeerAddressPrefix(String addressPrefix) {
        inner().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withSecondaryPeerAddressPrefix(String addressPrefix) {
        inner().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withVlanId(int vlanId) {
        inner().withVlanId(vlanId);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withPeerAsn(long peerASN) {
        inner().withPeerAsn(peerASN);
        return this;
    }

    @Override
    public DefinitionStages.WithCreate withSharedKey(String sharedKey) {
        inner().withSharedKey(sharedKey);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl defineIpv6Config() {
        return new Ipv6PeeringConfigImpl(new Ipv6ExpressRouteCircuitPeeringConfig(), this);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withoutIpv6Config() {
        inner().withIpv6PeeringConfig(null);
        return this;
    }

    ExpressRouteCrossConnectionPeeringImpl attachIpv6Config(Ipv6PeeringConfigImpl ipv6PeeringConfig) {
        inner().withIpv6PeeringConfig(ipv6PeeringConfig.inner());
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withCustomerAsn(int customerASN) {
        ensureMicrosoftPeeringConfig().withCustomerAsn(customerASN);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withRoutingRegistryName(String routingRegistryName) {
        ensureMicrosoftPeeringConfig().withRoutingRegistryName(routingRegistryName);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withState(ExpressRoutePeeringState state) {
        inner().withState(state);
        return this;
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionPeeringInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public Mono<ExpressRouteCrossConnectionPeering> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), inner())
            .map(
                innerModel -> {
                    ExpressRouteCrossConnectionPeeringImpl.this.setInner(innerModel);
                    parent.refresh();
                    return ExpressRouteCrossConnectionPeeringImpl.this;
                });
    }

    // Getters

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public NetworkManager manager() {
        return parent.manager();
    }

    @Override
    public String resourceGroupName() {
        return parent.resourceGroupName();
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
    public int azureAsn() {
        return Utils.toPrimitiveInt(inner().azureAsn());
    }

    @Override
    public long peerAsn() {
        return Utils.toPrimitiveLong(inner().peerAsn());
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
    public String provisioningState() {
        return inner().provisioningState().toString();
    }

    @Override
    public String gatewayManagerEtag() {
        return inner().gatewayManagerEtag();
    }

    @Override
    public String lastModifiedBy() {
        return inner().lastModifiedBy();
    }

    @Override
    public Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig() {
        return inner().ipv6PeeringConfig();
    }
}
