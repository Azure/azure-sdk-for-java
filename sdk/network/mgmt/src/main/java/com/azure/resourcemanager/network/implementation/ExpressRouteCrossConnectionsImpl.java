// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ExpressRouteCrossConnectionsClient;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCrossConnectionInner;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnections;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;

public class ExpressRouteCrossConnectionsImpl
    extends ReadableWrappersImpl<
        ExpressRouteCrossConnection, ExpressRouteCrossConnectionImpl, ExpressRouteCrossConnectionInner>
    implements ExpressRouteCrossConnections {
    private final NetworkManager manager;

    public ExpressRouteCrossConnectionsImpl(NetworkManager manager) {
        this.manager = manager;
    }

    @Override
    protected ExpressRouteCrossConnectionImpl wrapModel(ExpressRouteCrossConnectionInner inner) {
        if (inner == null) {
            return null;
        }
        return new ExpressRouteCrossConnectionImpl(inner.name(), inner, this.manager());
    }

    @Override
    public ExpressRouteCrossConnection getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<ExpressRouteCrossConnection> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }

    @Override
    public ExpressRouteCrossConnection getByResourceGroup(String resourceGroupName, String name) {
        return getByResourceGroupAsync(resourceGroupName, name).block();
    }

    @Override
    public Mono<ExpressRouteCrossConnection> getByResourceGroupAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name).map(inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<ExpressRouteCrossConnection> listByResourceGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedFlux<ExpressRouteCrossConnection> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public NetworkManager manager() {
        return manager;
    }

    @Override
    public PagedIterable<ExpressRouteCrossConnection> list() {
        return wrapList(inner().list());
    }

    @Override
    public PagedFlux<ExpressRouteCrossConnection> listAsync() {
        return wrapPageAsync(inner().listAsync());
    }

    @Override
    public ExpressRouteCrossConnectionsClient inner() {
        return manager.inner().getExpressRouteCrossConnections();
    }
}
