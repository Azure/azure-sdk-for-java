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
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitAuthorizationInner;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitInner;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
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
        inner().withSku(sku.sku());
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withClassicOperations() {
        inner().withAllowClassicOperations(true);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withoutClassicOperations() {
        inner().withAllowClassicOperations(false);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withAuthorization(String authorizationName) {
        ensureAuthorizations().add(new ExpressRouteCircuitAuthorizationInner().withName(authorizationName));
        return this;
    }

    private List<ExpressRouteCircuitAuthorizationInner> ensureAuthorizations() {
        if (inner().authorizations() == null) {
            inner().withAuthorizations(new ArrayList<ExpressRouteCircuitAuthorizationInner>());
        }
        return inner().authorizations();
    }

    private ExpressRouteCircuitServiceProviderProperties ensureServiceProviderProperties() {
        if (inner().serviceProviderProperties() == null) {
            inner().withServiceProviderProperties(new ExpressRouteCircuitServiceProviderProperties());
        }
        return inner().serviceProviderProperties();
    }

    @Override
    protected Mono<ExpressRouteCircuitInner> createInner() {
        return this
            .manager()
            .inner()
            .getExpressRouteCircuits()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void initializeChildrenFromInner() {
        expressRouteCircuitPeerings = new HashMap<>();
        if (inner().peerings() != null) {
            for (ExpressRouteCircuitPeeringInner peering : inner().peerings()) {
                expressRouteCircuitPeerings
                    .put(
                        peering.name(),
                        new ExpressRouteCircuitPeeringImpl<>(this, peering,
                            manager().inner().getExpressRouteCircuitPeerings(), peering.peeringType()));
            }
        }
    }

    @Override
    protected Mono<ExpressRouteCircuitInner> getInnerAsync() {
        return this
            .manager()
            .inner()
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
            .inner()
            .getExpressRouteCircuits()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags());
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
        return ExpressRouteCircuitSkuType.fromSku(inner().sku());
    }

    @Override
    public boolean isAllowClassicOperations() {
        return Utils.toPrimitiveBoolean(inner().allowClassicOperations());
    }

    @Override
    public String circuitProvisioningState() {
        return inner().circuitProvisioningState();
    }

    @Override
    public ServiceProviderProvisioningState serviceProviderProvisioningState() {
        return inner().serviceProviderProvisioningState();
    }

    @Override
    public String serviceKey() {
        return inner().serviceKey();
    }

    @Override
    public String serviceProviderNotes() {
        return inner().serviceProviderNotes();
    }

    @Override
    public ExpressRouteCircuitServiceProviderProperties serviceProviderProperties() {
        return inner().serviceProviderProperties();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState().toString();
    }

    @Override
    public Map<String, ExpressRouteCircuitPeering> peeringsMap() {
        return expressRouteCircuitPeerings;
    }
}
