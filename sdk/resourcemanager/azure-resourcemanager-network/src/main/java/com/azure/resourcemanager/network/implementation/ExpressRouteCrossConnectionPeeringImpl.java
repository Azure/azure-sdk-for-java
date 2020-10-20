// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCrossConnectionPeeringsClient;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringState;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringType;
import com.azure.resourcemanager.network.models.Ipv6ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
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
        this.client = parent.manager().serviceClient().getExpressRouteCrossConnectionPeerings();
        this.parent = parent;
        innerModel().withPeeringType(type);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withAdvertisedPublicPrefixes(String publicPrefix) {
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
    public ExpressRouteCrossConnectionPeeringImpl withPrimaryPeerAddressPrefix(String addressPrefix) {
        innerModel().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withSecondaryPeerAddressPrefix(String addressPrefix) {
        innerModel().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withVlanId(int vlanId) {
        innerModel().withVlanId(vlanId);
        return this;
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withPeerAsn(long peerASN) {
        innerModel().withPeerAsn(peerASN);
        return this;
    }

    @Override
    public DefinitionStages.WithCreate withSharedKey(String sharedKey) {
        innerModel().withSharedKey(sharedKey);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl defineIpv6Config() {
        return new Ipv6PeeringConfigImpl(new Ipv6ExpressRouteCircuitPeeringConfig(), this);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl withoutIpv6Config() {
        innerModel().withIpv6PeeringConfig(null);
        return this;
    }

    ExpressRouteCrossConnectionPeeringImpl attachIpv6Config(Ipv6PeeringConfigImpl ipv6PeeringConfig) {
        innerModel().withIpv6PeeringConfig(ipv6PeeringConfig.innerModel());
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
        innerModel().withState(state);
        return this;
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionPeeringInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public Mono<ExpressRouteCrossConnectionPeering> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), innerModel())
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
        return innerModel().id();
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
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public String gatewayManagerEtag() {
        return innerModel().gatewayManagerEtag();
    }

    @Override
    public String lastModifiedBy() {
        return innerModel().lastModifiedBy();
    }

    @Override
    public Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig() {
        return innerModel().ipv6PeeringConfig();
    }
}
