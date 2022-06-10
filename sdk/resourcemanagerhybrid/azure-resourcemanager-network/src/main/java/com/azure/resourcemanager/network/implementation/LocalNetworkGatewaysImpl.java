// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.LocalNetworkGatewaysClient;
import com.azure.resourcemanager.network.fluent.models.LocalNetworkGatewayInner;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.LocalNetworkGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

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
        super(networkManager.serviceClient().getLocalNetworkGateways(), networkManager);
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
        return PagedConverter.mapPage(PagedConverter.mergePagedFlux(this.manager().resourceManager().resourceGroups().listAsync(),
            rg -> inner().listByResourceGroupAsync(rg.name())), this::wrapModel);
    }

    @Override
    public PagedIterable<LocalNetworkGateway> listByResourceGroup(String groupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(groupName));
    }

    @Override
    public PagedFlux<LocalNetworkGateway> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
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
