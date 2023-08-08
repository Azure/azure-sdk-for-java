// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VirtualNetworkGatewaysClient;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkGatewayInner;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

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
        super(networkManager.serviceClient().getVirtualNetworkGateways(), networkManager);
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
        return PagedConverter.mapPage(PagedConverter.mergePagedFlux(this.manager().resourceManager().resourceGroups().listAsync(),
            rg -> inner().listByResourceGroupAsync(rg.name())), this::wrapModel);
    }

    @Override
    public PagedIterable<VirtualNetworkGateway> listByResourceGroup(String groupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(groupName));
    }

    @Override
    public PagedFlux<VirtualNetworkGateway> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
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
