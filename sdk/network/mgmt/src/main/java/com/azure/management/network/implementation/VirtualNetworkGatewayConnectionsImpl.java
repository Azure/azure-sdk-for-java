/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewayConnection;
import com.azure.management.network.VirtualNetworkGatewayConnections;
import com.azure.management.network.models.VirtualNetworkGatewayConnectionInner;
import com.azure.management.network.models.VirtualNetworkGatewayConnectionsInner;
import com.azure.management.network.models.VirtualNetworkGatewayInner;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * The implementation of VirtualNetworkGatewayConnections.
 */
class VirtualNetworkGatewayConnectionsImpl
        extends GroupableResourcesImpl<
        VirtualNetworkGatewayConnection,
        VirtualNetworkGatewayConnectionImpl,
        VirtualNetworkGatewayConnectionInner,
        VirtualNetworkGatewayConnectionsInner,
        NetworkManager>
        implements VirtualNetworkGatewayConnections {

    private final VirtualNetworkGatewayImpl parent;

    VirtualNetworkGatewayConnectionsImpl(final VirtualNetworkGatewayImpl parent) {
        super(parent.manager().inner().virtualNetworkGatewayConnections(), parent.manager());
        this.parent = parent;
    }


    @Override
    protected VirtualNetworkGatewayConnectionImpl wrapModel(String name) {
        return new VirtualNetworkGatewayConnectionImpl(name, parent, new VirtualNetworkGatewayConnectionInner())
                .withRegion(parent.regionName())
                .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected VirtualNetworkGatewayConnectionImpl wrapModel(VirtualNetworkGatewayConnectionInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualNetworkGatewayConnectionImpl(inner.getName(), parent, inner);
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.inner().deleteAsync(parent.resourceGroupName(), name);
    }

    @Override
    public PagedIterable<VirtualNetworkGatewayConnection> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public VirtualNetworkGatewayConnection getByName(String name) {
        VirtualNetworkGatewayConnectionInner inner = this.manager().inner().virtualNetworkGatewayConnections()
                .getByResourceGroup(this.parent().resourceGroupName(), name);
        return new VirtualNetworkGatewayConnectionImpl(name, parent, inner);
    }

    @Override
    public VirtualNetworkGateway parent() {
        return this.parent;
    }

    @Override
    public PagedFlux<VirtualNetworkGatewayConnection> listAsync() {
        PagedIterable<ResourceGroup> resources = new PagedIterable<>(this.manager().getResourceManager().resourceGroups().listAsync());
        Iterator<ResourceGroup> iterator = resources.iterator();

        Function<String, Mono<PagedResponse<VirtualNetworkGatewayConnectionInner>>> fetcher = (continuation) -> {
            if (continuation == null) {
                if (iterator.hasNext()) {
                    return inner().listByResourceGroupSinglePageAsync(iterator.next().name());
                } else {
                    return Mono.empty();
                }
            } else {
                return inner().listByResourceGroupSinglePageAsync(continuation);
            }
        };

        return new PagedFlux<>(() -> fetcher.apply(null), (continuation) -> fetcher.apply(continuation)).mapPage(inner -> wrapModel(inner));
    }

    @Override
    protected Mono<VirtualNetworkGatewayConnectionInner> getInnerAsync(String resourceGroupName, String name) {
        return inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public Mono<VirtualNetworkGatewayConnection> getByNameAsync(String name) {
        return inner().getByResourceGroupAsync(parent.resourceGroupName(), name)
                .map(inner -> wrapModel(inner));
    }
}