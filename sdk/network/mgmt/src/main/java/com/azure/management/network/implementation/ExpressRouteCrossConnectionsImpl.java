/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.ExpressRouteCrossConnection;
import com.azure.management.network.ExpressRouteCrossConnections;
import com.azure.management.network.models.ExpressRouteCrossConnectionInner;
import com.azure.management.network.models.ExpressRouteCrossConnectionsInner;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;

class ExpressRouteCrossConnectionsImpl extends
        ReadableWrappersImpl<ExpressRouteCrossConnection, ExpressRouteCrossConnectionImpl, ExpressRouteCrossConnectionInner>
        implements ExpressRouteCrossConnections {
    private final NetworkManager manager;

    ExpressRouteCrossConnectionsImpl(NetworkManager manager) {
        this.manager = manager;
    }

    @Override
    protected ExpressRouteCrossConnectionImpl wrapModel(ExpressRouteCrossConnectionInner inner) {
        if (inner == null) {
            return null;
        }
        return new ExpressRouteCrossConnectionImpl(inner.getName(), inner, this.manager());
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
    public ExpressRouteCrossConnectionsInner inner() {
        return manager.inner().expressRouteCrossConnections();
    }
}
