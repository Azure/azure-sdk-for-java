// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VirtualNetworkGatewayConnectionsClient;
import com.azure.resourcemanager.network.fluent.models.ConnectionSharedKeyInner;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkGatewayConnectionInner;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnections;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/** The implementation of VirtualNetworkGatewayConnections. */
class VirtualNetworkGatewayConnectionsImpl
    extends GroupableResourcesImpl<
        VirtualNetworkGatewayConnection,
        VirtualNetworkGatewayConnectionImpl,
        VirtualNetworkGatewayConnectionInner,
        VirtualNetworkGatewayConnectionsClient,
        NetworkManager>
    implements VirtualNetworkGatewayConnections {

    private final VirtualNetworkGatewayImpl parent;

    VirtualNetworkGatewayConnectionsImpl(final VirtualNetworkGatewayImpl parent) {
        super(parent.manager().serviceClient().getVirtualNetworkGatewayConnections(), parent.manager());
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
        return new VirtualNetworkGatewayConnectionImpl(inner.name(), parent, inner);
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
        VirtualNetworkGatewayConnectionInner inner =
            this
                .manager()
                .serviceClient()
                .getVirtualNetworkGatewayConnections()
                .getByResourceGroup(this.parent().resourceGroupName(), name);
        return new VirtualNetworkGatewayConnectionImpl(name, parent, inner);
    }

    @Override
    public VirtualNetworkGateway parent() {
        return this.parent;
    }

    @Override
    public PagedFlux<VirtualNetworkGatewayConnection> listAsync() {
        return PagedConverter.mapPage(PagedConverter.mergePagedFlux(this.manager().resourceManager().resourceGroups().listAsync(), rg ->
            inner().listByResourceGroupAsync(rg.name())), this::wrapModel);
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
        return inner().getByResourceGroupAsync(parent.resourceGroupName(), name).map(this::wrapModel);
    }

    @Override
    public String getSharedKeyById(String id) {
        return getSharedKeyByIdAsync(id).block();
    }

    @Override
    public Mono<String> getSharedKeyByIdAsync(String id) {
        return inner().getSharedKeyAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id))
            .map(ConnectionSharedKeyInner::value);
    }

    @Override
    public String setSharedKeyById(String id, String sharedKey) {
        return setSharedKeyByIdAsync(id, sharedKey).block();
    }

    @Override
    public Mono<String> setSharedKeyByIdAsync(String id, String sharedKey) {
        return setSharedKeyByNameAsync(ResourceUtils.nameFromResourceId(id), sharedKey);
    }

    @Override
    public String setSharedKeyByName(String name, String sharedKey) {
        return setSharedKeyByNameAsync(name, sharedKey).block();
    }

    @Override
    public Mono<String> setSharedKeyByNameAsync(String name, String sharedKey) {
        return inner().setSharedKeyAsync(
            this.parent().resourceGroupName(), name, new ConnectionSharedKeyInner().withValue(sharedKey))
            .map(ConnectionSharedKeyInner::value);
    }
}
