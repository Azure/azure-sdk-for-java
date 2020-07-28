// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VirtualNetworkGatewaysClient;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayInner;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.function.Function;

/** Implementation for VirtualNetworkGateways. */
public class VirtualNetworkGatewaysImpl
    extends GroupableResourcesImpl<
        VirtualNetworkGateway,
        VirtualNetworkGatewayImpl,
        VirtualNetworkGatewayInner,
    VirtualNetworkGatewaysClient,
    NetworkManager>
    implements VirtualNetworkGateways {

    public VirtualNetworkGatewaysImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getVirtualNetworkGateways(), networkManager);
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
        PagedIterable<ResourceGroup> resources =
            new PagedIterable<>(this.manager().resourceManager().resourceGroups().listAsync());
        Iterator<ResourceGroup> iterator = resources.iterator();

        Function<String, Mono<PagedResponse<VirtualNetworkGatewayInner>>> fetcher =
            (continuation) -> {
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

        return new PagedFlux<>(() -> fetcher.apply(null), (continuation) -> fetcher.apply(continuation))
            .mapPage(inner -> wrapModel(inner));
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
        return new VirtualNetworkGatewayImpl(inner.name(), inner, this.manager());
    }
}
