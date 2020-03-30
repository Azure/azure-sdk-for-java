/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.ExpressRouteCircuit;
import com.azure.management.network.ExpressRouteCircuitPeering;
import com.azure.management.network.ExpressRouteCircuitPeerings;
import com.azure.management.network.ExpressRoutePeeringType;
import com.azure.management.network.models.ExpressRouteCircuitPeeringInner;
import com.azure.management.network.models.ExpressRouteCircuitPeeringsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import reactor.core.publisher.Mono;

/**
 * Represents Express Route Circuit Peerings collection associated with Network Watcher.
 */
class ExpressRouteCircuitPeeringsImpl extends IndependentChildrenImpl<
        ExpressRouteCircuitPeering,
        ExpressRouteCircuitPeeringImpl,
        ExpressRouteCircuitPeeringInner,
        ExpressRouteCircuitPeeringsInner,
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
        super(parent.manager().inner().expressRouteCircuitPeerings(), parent.manager());
        this.parent = parent;
    }

    @Override
    public final PagedIterable<ExpressRouteCircuitPeering> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /**
     * @return an observable emits packet captures in this collection
     */
    @Override
    public PagedFlux<ExpressRouteCircuitPeering> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ExpressRouteCircuitPeeringImpl wrapModel(String name) {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.fromString(name));
    }

    protected ExpressRouteCircuitPeeringImpl wrapModel(ExpressRouteCircuitPeeringInner inner) {
        return (inner == null) ? null : new ExpressRouteCircuitPeeringImpl(parent, inner, inner(), inner.peeringType());
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineAzurePrivatePeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.AZURE_PRIVATE_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineAzurePublicPeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.AZURE_PUBLIC_PEERING);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl defineMicrosoftPeering() {
        return new ExpressRouteCircuitPeeringImpl(parent, new ExpressRouteCircuitPeeringInner(), inner(), ExpressRoutePeeringType.MICROSOFT_PEERING);
    }

    @Override
    public Mono<ExpressRouteCircuitPeering> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name)
                .map(inner -> wrapModel(inner));
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
        return this.inner().deleteAsync(parent.resourceGroupName(),
                parent.name(),
                name);
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
        return inner().getAsync(resourceGroup, parentName, name)
                .map(inner -> wrapModel((inner)));
    }

    @Override
    public PagedIterable<ExpressRouteCircuitPeering> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.inner().list(resourceGroupName, parentName));
    }
}