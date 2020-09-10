// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.cosmos.fluent.PrivateEndpointConnectionsClient;
import com.azure.resourcemanager.cosmos.fluent.inner.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a private endpoint connection collection. */
class PrivateEndpointConnectionsImpl
    extends ExternalChildResourcesCachedImpl<
        PrivateEndpointConnectionImpl,
        PrivateEndpointConnection,
        PrivateEndpointConnectionInner,
        CosmosDBAccountImpl,
        CosmosDBAccount> {
    private final PrivateEndpointConnectionsClient client;

    PrivateEndpointConnectionsImpl(PrivateEndpointConnectionsClient client, CosmosDBAccountImpl parent) {
        super(parent, parent.taskGroup(), "PrivateEndpointConnection");
        this.client = client;
    }

    public PrivateEndpointConnectionImpl define(String name) {
        return this.prepareInlineDefine(name);
    }

    public PrivateEndpointConnectionImpl update(String name) {
        if (this.collection().size() == 0) {
            this.cacheCollection();
        }
        return this.prepareInlineUpdate(name);
    }

    public void remove(String name) {
        if (this.collection().size() == 0) {
            this.cacheCollection();
        }
        this.prepareInlineRemove(name);
    }

    public Map<String, PrivateEndpointConnection> asMap() {
        return asMapAsync().block();
    }

    public Mono<Map<String, PrivateEndpointConnection>> asMapAsync() {
        return listAsync()
            .collectList()
            .map(
                privateEndpointConnections -> {
                    Map<String, PrivateEndpointConnection> privateEndpointConnectionMap = new HashMap<>();
                    for (PrivateEndpointConnectionImpl privateEndpointConnection : privateEndpointConnections) {
                        privateEndpointConnectionMap.put(privateEndpointConnection.name(), privateEndpointConnection);
                    }
                    return privateEndpointConnectionMap;
                });
    }

    public PagedFlux<PrivateEndpointConnectionImpl> listAsync() {
        final PrivateEndpointConnectionsImpl self = this;
        return this
            .client
            .listByDatabaseAccountAsync(this.getParent().resourceGroupName(), this.getParent().name())
            .mapPage(
                inner -> {
                    PrivateEndpointConnectionImpl childResource =
                        new PrivateEndpointConnectionImpl(inner.name(), self.getParent(), inner, client);
                    self.addPrivateEndpointConnection(childResource);
                    return childResource;
                });
    }

    public Mono<PrivateEndpointConnectionImpl> getImplAsync(String name) {
        final PrivateEndpointConnectionsImpl self = this;
        return this
            .client
            .getAsync(getParent().resourceGroupName(), getParent().name(), name)
            .map(
                inner -> {
                    PrivateEndpointConnectionImpl childResource =
                        new PrivateEndpointConnectionImpl(inner.name(), getParent(), inner, client);
                    self.addPrivateEndpointConnection(childResource);
                    return childResource;
                });
    }

    @Override
    protected Flux<PrivateEndpointConnectionImpl> listChildResourcesAsync() {
        return listAsync();
    }

    @Override
    protected List<PrivateEndpointConnectionImpl> listChildResources() {
        return listAsync().collectList().block();
    }

    @Override
    protected PrivateEndpointConnectionImpl newChildResource(String name) {
        return new PrivateEndpointConnectionImpl(name, getParent(), new PrivateEndpointConnectionInner(), this.client);
    }

    public void addPrivateEndpointConnection(PrivateEndpointConnectionImpl privateEndpointConnection) {
        this.addChildResource(privateEndpointConnection);
    }
}
