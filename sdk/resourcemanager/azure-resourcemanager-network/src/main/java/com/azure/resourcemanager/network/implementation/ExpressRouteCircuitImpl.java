// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ExpressRouteCircuit;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeerings;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitServiceProviderProperties;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitSkuType;
import com.azure.resourcemanager.network.models.ServiceProviderProvisioningState;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitAuthorizationInner;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitInner;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

class ExpressRouteCircuitImpl
    extends GroupableParentResourceWithTagsImpl<
        ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl, NetworkManager>
    implements ExpressRouteCircuit, ExpressRouteCircuit.Definition, ExpressRouteCircuit.Update {
    private ExpressRouteCircuitPeeringsImpl peerings;
    private Map<String, ExpressRouteCircuitPeering> expressRouteCircuitPeerings;

    ExpressRouteCircuitImpl(String name, ExpressRouteCircuitInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    @Override
    public ExpressRouteCircuitImpl withServiceProvider(String serviceProviderName) {
        ensureServiceProviderProperties().withServiceProviderName(serviceProviderName);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withPeeringLocation(String location) {
        ensureServiceProviderProperties().withPeeringLocation(location);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withBandwidthInMbps(int bandwidthInMbps) {
        ensureServiceProviderProperties().withBandwidthInMbps(bandwidthInMbps);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withSku(ExpressRouteCircuitSkuType sku) {
        innerModel().withSku(sku.sku());
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withClassicOperations() {
        innerModel().withAllowClassicOperations(true);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withoutClassicOperations() {
        innerModel().withAllowClassicOperations(false);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withAuthorization(String authorizationName) {
        ensureAuthorizations().add(new ExpressRouteCircuitAuthorizationInner().withName(authorizationName));
        return this;
    }

    private List<ExpressRouteCircuitAuthorizationInner> ensureAuthorizations() {
        if (innerModel().authorizations() == null) {
            innerModel().withAuthorizations(new ArrayList<ExpressRouteCircuitAuthorizationInner>());
        }
        return innerModel().authorizations();
    }

    private ExpressRouteCircuitServiceProviderProperties ensureServiceProviderProperties() {
        if (innerModel().serviceProviderProperties() == null) {
            innerModel().withServiceProviderProperties(new ExpressRouteCircuitServiceProviderProperties());
        }
        return innerModel().serviceProviderProperties();
    }

    @Override
    protected Mono<ExpressRouteCircuitInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getExpressRouteCircuits()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }

    @Override
    protected void initializeChildrenFromInner() {
        expressRouteCircuitPeerings = new HashMap<>();
        if (innerModel().peerings() != null) {
            for (ExpressRouteCircuitPeeringInner peering : innerModel().peerings()) {
                expressRouteCircuitPeerings
                    .put(
                        peering.name(),
                        new ExpressRouteCircuitPeeringImpl<>(
                            this,
                            peering,
                            manager().serviceClient().getExpressRouteCircuitPeerings(),
                            peering.peeringType()));
            }
        }
    }

    @Override
    protected Mono<ExpressRouteCircuitInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getExpressRouteCircuits()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ExpressRouteCircuit> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                expressRouteCircuit -> {
                    ExpressRouteCircuitImpl impl = (ExpressRouteCircuitImpl) expressRouteCircuit;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<ExpressRouteCircuitInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getExpressRouteCircuits()
            .updateTagsAsync(resourceGroupName(), name(), innerModel().tags());
    }

    // Getters

    @Override
    public ExpressRouteCircuitPeerings peerings() {
        if (peerings == null) {
            peerings = new ExpressRouteCircuitPeeringsImpl(this);
        }
        return peerings;
    }

    @Override
    public ExpressRouteCircuitSkuType sku() {
        return ExpressRouteCircuitSkuType.fromSku(innerModel().sku());
    }

    @Override
    public boolean isAllowClassicOperations() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().allowClassicOperations());
    }

    @Override
    public String circuitProvisioningState() {
        return innerModel().circuitProvisioningState();
    }

    @Override
    public ServiceProviderProvisioningState serviceProviderProvisioningState() {
        return innerModel().serviceProviderProvisioningState();
    }

    @Override
    public String serviceKey() {
        return innerModel().serviceKey();
    }

    @Override
    public String serviceProviderNotes() {
        return innerModel().serviceProviderNotes();
    }

    @Override
    public ExpressRouteCircuitServiceProviderProperties serviceProviderProperties() {
        return innerModel().serviceProviderProperties();
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public Map<String, ExpressRouteCircuitPeering> peeringsMap() {
        return expressRouteCircuitPeerings;
    }
}
