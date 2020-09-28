// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCrossConnectionPeeringsClient;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeerings;
import com.azure.resourcemanager.network.models.ExpressRoutePeeringType;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import reactor.core.publisher.Mono;

/** Represents Express Route Cross Connection Peerings collection associated with Network Watcher. */
class ExpressRouteCrossConnectionPeeringsImpl
    extends IndependentChildrenImpl<
        ExpressRouteCrossConnectionPeering,
        ExpressRouteCrossConnectionPeeringImpl,
        ExpressRouteCrossConnectionPeeringInner,
        ExpressRouteCrossConnectionPeeringsClient,
        NetworkManager,
        ExpressRouteCrossConnection>
    implements ExpressRouteCrossConnectionPeerings {
    private final ExpressRouteCrossConnectionImpl parent;

    /**
     * Creates a new ExpressRouteCrossConnectionPeeringsImpl.
     *
     * @param parent the Express Route Circuit associated with ExpressRouteCrossConnectionPeering
     */
    ExpressRouteCrossConnectionPeeringsImpl(ExpressRouteCrossConnectionImpl parent) {
        super(parent.manager().serviceClient().getExpressRouteCrossConnectionPeerings(), parent.manager());
        this.parent = parent;
    }

    @Override
    public final PagedIterable<ExpressRouteCrossConnectionPeering> list() {
        return wrapList(innerModel().list(parent.resourceGroupName(), parent.name()));
    }

    /** @return an observable emits packet captures in this collection */
    @Override
    public PagedFlux<ExpressRouteCrossConnectionPeering> listAsync() {
        return wrapPageAsync(innerModel().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ExpressRouteCrossConnectionPeeringImpl wrapModel(String name) {
        return new ExpressRouteCrossConnectionPeeringImpl(
            parent, new ExpressRouteCrossConnectionPeeringInner(), ExpressRoutePeeringType.fromString(name));
    }

    protected ExpressRouteCrossConnectionPeeringImpl wrapModel(ExpressRouteCrossConnectionPeeringInner inner) {
        return (inner == null) ? null : new ExpressRouteCrossConnectionPeeringImpl(parent, inner, inner.peeringType());
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl defineAzurePrivatePeering() {
        return new ExpressRouteCrossConnectionPeeringImpl(
            parent, new ExpressRouteCrossConnectionPeeringInner(), ExpressRoutePeeringType.AZURE_PRIVATE_PEERING);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl defineMicrosoftPeering() {
        return new ExpressRouteCrossConnectionPeeringImpl(
            parent, new ExpressRouteCrossConnectionPeeringInner(), ExpressRoutePeeringType.MICROSOFT_PEERING);
    }

    @Override
    public Mono<ExpressRouteCrossConnectionPeering> getByNameAsync(String name) {
        return innerModel().getAsync(parent.resourceGroupName(), parent.name(), name).map(inner -> wrapModel(inner));
    }

    @Override
    public ExpressRouteCrossConnectionPeering getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return deleteByParentAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public ExpressRouteCrossConnection parent() {
        return parent;
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return this
            .innerModel()
            .deleteAsync(groupName, parentName, name)
            .doOnSuccess(
                result -> {
                    parent.refresh();
                })
            .then();
    }

    @Override
    public Mono<ExpressRouteCrossConnectionPeering> getByParentAsync(
        String resourceGroup, String parentName, String name) {
        return innerModel().getAsync(resourceGroup, parentName, name).map(inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<ExpressRouteCrossConnectionPeering> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.innerModel().list(resourceGroupName, parentName));
    }
}
