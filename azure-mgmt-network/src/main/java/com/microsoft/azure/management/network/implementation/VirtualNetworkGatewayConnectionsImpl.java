/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnection;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnections;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

import java.util.List;

/**
 * The implementation of VirtualNetworkGatewayConnections.
 */
@LangDefinition
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
        return wrapModel(inner);
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAsync(parent.resourceGroupName(), name, callback);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAsync(parent.resourceGroupName(), name).toCompletable();
    }

    @Override
    public PagedList<VirtualNetworkGatewayConnection> list() {
        // TODO
        return new GroupPagedList<VirtualNetworkGatewayConnection>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<VirtualNetworkGatewayConnection> listNextGroup(String resourceGroupName) {
                return wrapList(VirtualNetworkGatewayConnectionsImpl.this.inner().listByResourceGroup(resourceGroupName));
            }
        };
    }

    @Override
    public VirtualNetworkGatewayConnection getByName(String name) {
        VirtualNetworkGatewayConnectionInner inner = this.parent().manager().inner().virtualNetworkGatewayConnections()
                .getByResourceGroup(this.parent().resourceGroupName(), name);
        return new VirtualNetworkGatewayConnectionImpl(name, parent, inner);
    }

    @Override
    public VirtualNetworkGateway parent() {
        return this.parent;
    }

    @Override
    public Observable<VirtualNetworkGatewayConnection> listAsync() {
        return null;
    }

    @Override
    protected Observable<VirtualNetworkGatewayConnectionInner> getInnerAsync(String resourceGroupName, String name) {
        return null;
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return null;
    }
}