// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.LocalNetworkGatewaysClient;
import com.azure.resourcemanager.network.fluent.inner.LocalNetworkGatewayInner;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.LocalNetworkGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.function.Function;

/** Implementation for LocalNetworkGateways. */
public class LocalNetworkGatewaysImpl
    extends GroupableResourcesImpl<
        LocalNetworkGateway,
        LocalNetworkGatewayImpl,
        LocalNetworkGatewayInner,
    LocalNetworkGatewaysClient,
    NetworkManager>
    implements LocalNetworkGateways {

    public LocalNetworkGatewaysImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getLocalNetworkGateways(), networkManager);
    }

    @Override
    public LocalNetworkGatewayImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<LocalNetworkGateway> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<LocalNetworkGateway> listAsync() {
        PagedIterable<ResourceGroup> resources =
            new PagedIterable<>(this.manager().resourceManager().resourceGroups().listAsync());
        Iterator<ResourceGroup> iterator = resources.iterator();

        Function<String, Mono<PagedResponse<LocalNetworkGatewayInner>>> fetcher =
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
    public PagedIterable<LocalNetworkGateway> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    public PagedFlux<LocalNetworkGateway> listByResourceGroupAsync(String groupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(groupName));
    }

    @Override
    protected Mono<LocalNetworkGatewayInner> getInnerAsync(String groupName, String name) {
        return this.inner().getByResourceGroupAsync(groupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    // Fluent model create helpers

    @Override
    protected LocalNetworkGatewayImpl wrapModel(String name) {
        LocalNetworkGatewayInner inner = new LocalNetworkGatewayInner();

        return new LocalNetworkGatewayImpl(name, inner, super.manager());
    }

    @Override
    protected LocalNetworkGatewayImpl wrapModel(LocalNetworkGatewayInner inner) {
        if (inner == null) {
            return null;
        }
        return new LocalNetworkGatewayImpl(inner.name(), inner, this.manager());
    }
}
