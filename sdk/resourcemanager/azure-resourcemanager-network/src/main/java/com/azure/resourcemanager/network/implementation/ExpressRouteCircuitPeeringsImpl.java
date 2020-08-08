// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCircuitPeeringsClient;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitInner;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuit;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeerings;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringType;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import reactor.core.publisher.Mono;

/** Represents Express Route Circuit Peerings collection associated with Network Watcher. */
class ExpressRouteCircuitPeeringsImpl
    extends IndependentChildrenImpl<
        ExpressRouteCircuitPeering,
        ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>,
        ExpressRouteCircuitPeeringInner,
        ExpressRouteCircuitPeeringsClient,
    NetworkManager,
        ExpressRouteCircuit>
    implements ExpressRouteCircuitPeerings {
    private final ExpressRouteCircuitImpl parent;

    /**
     * Creates a new ExpressRouteCircuitPeeringsImpl.
     *
     * @param parent the Express Route Circuit associated with ExpressRouteCircuitPeering
     */
    ExpressRouteCircuitPeeringsImpl(ExpressRouteCircuitImpl parent) {
        super(parent.manager().inner().getExpressRouteCircuitPeerings(), parent.manager());
        this.parent = parent;
    }

    @Override
    public final PagedIterable<ExpressRouteCircuitPeering> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /** @return an observable emits packet captures in this collection */
    @Override
    public PagedFlux<ExpressRouteCircuitPeering> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>
        wrapModel(String name) {
        return new ExpressRouteCircuitPeeringImpl<>(
            parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.fromString(name));
    }

    protected ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>
        wrapModel(ExpressRouteCircuitPeeringInner inner) {
        return (inner == null) ? null
            : new ExpressRouteCircuitPeeringImpl<>(parent, inner, inner(), inner.peeringType());
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>
        defineAzurePrivatePeering() {
        return new ExpressRouteCircuitPeeringImpl<>(
            parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.AZURE_PRIVATE_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>
        defineAzurePublicPeering() {
        return new ExpressRouteCircuitPeeringImpl<>(
            parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.AZURE_PUBLIC_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl<ExpressRouteCircuit, ExpressRouteCircuitInner, ExpressRouteCircuitImpl>
        defineMicrosoftPeering() {
        return new ExpressRouteCircuitPeeringImpl<>(
            parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.MICROSOFT_PEERING);
    }

    @Override
    public Mono<ExpressRouteCircuitPeering> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name).map(inner -> wrapModel(inner));
    }

    @Override
    public ExpressRouteCircuitPeering getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.inner().deleteAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public ExpressRouteCircuit parent() {
        return parent;
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return this.inner().deleteAsync(groupName, parentName, name);
    }

    @Override
    public Mono<ExpressRouteCircuitPeering> getByParentAsync(String resourceGroup, String parentName, String name) {
        return inner().getAsync(resourceGroup, parentName, name).map(inner -> wrapModel((inner)));
    }

    @Override
    public PagedIterable<ExpressRouteCircuitPeering> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.inner().list(resourceGroupName, parentName));
    }
}
