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
import com.azure.management.network.VirtualNetworkGateways;
import com.azure.management.network.models.VirtualNetworkGatewayInner;
import com.azure.management.network.models.VirtualNetworkGatewaysInner;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation for VirtualNetworkGateways.
 */
class VirtualNetworkGatewaysImpl
        extends GroupableResourcesImpl<
        VirtualNetworkGateway,
        VirtualNetworkGatewayImpl,
        VirtualNetworkGatewayInner,
        VirtualNetworkGatewaysInner,
        NetworkManager>
        implements VirtualNetworkGateways {

    VirtualNetworkGatewaysImpl(final NetworkManager networkManager) {
        super(networkManager.inner().virtualNetworkGateways(), networkManager);
    }

    @Override
    public VirtualNetworkGatewayImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<VirtualNetworkGateway> list() {
        return new PagedIterable<>(this.listAsync());
    }

    // TODO: Test this
    @Override
    public PagedFlux<VirtualNetworkGateway> listAsync() {
        PagedIterable<ResourceGroup> resources = new PagedIterable<>(this.manager().getResourceManager().resourceGroups().listAsync());
        Iterator<ResourceGroup> iterator = resources.iterator();

        Function<String, Mono<PagedResponse<VirtualNetworkGatewayInner>>> fetcher = (continuation) -> {
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
    public PagedIterable<VirtualNetworkGateway> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    public PagedFlux<VirtualNetworkGateway> listByResourceGroupAsync(String groupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(groupName));
    }

    @Override
    protected Mono<VirtualNetworkGatewayInner> getInnerAsync(String groupName, String name) {
        return this.inner().getByResourceGroupAsync(groupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    // Fluent model create helpers

    @Override
    protected VirtualNetworkGatewayImpl wrapModel(String name) {
        VirtualNetworkGatewayInner inner = new VirtualNetworkGatewayInner();

        return new VirtualNetworkGatewayImpl(name, inner, super.manager());
    }

    @Override
    protected VirtualNetworkGatewayImpl wrapModel(VirtualNetworkGatewayInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualNetworkGatewayImpl(inner.getName(), inner, this.manager());
    }
}

