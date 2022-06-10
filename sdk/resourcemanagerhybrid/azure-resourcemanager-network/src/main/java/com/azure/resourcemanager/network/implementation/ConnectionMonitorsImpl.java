// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.fluent.ConnectionMonitorsClient;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorResultInner;
import com.azure.resourcemanager.network.models.ConnectionMonitor;
import com.azure.resourcemanager.network.models.ConnectionMonitors;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import reactor.core.publisher.Mono;

/** Represents Connection Monitors collection associated with Network Watcher. */
class ConnectionMonitorsImpl
    extends CreatableResourcesImpl<ConnectionMonitor, ConnectionMonitorImpl, ConnectionMonitorResultInner>
    implements ConnectionMonitors {
    private final NetworkWatcherImpl parent;
    private final ConnectionMonitorsClient innerCollection;

    /**
     * Creates a new ConnectionMonitorsImpl.
     *
     * @param parent the Network Watcher associated with Connection Monitors
     */
    ConnectionMonitorsImpl(ConnectionMonitorsClient innerCollection, NetworkWatcherImpl parent) {
        this.parent = parent;
        this.innerCollection = innerCollection;
    }

    @Override
    public final PagedIterable<ConnectionMonitor> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /** @return an observable emits connection monitors in this collection */
    @Override
    public PagedFlux<ConnectionMonitor> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ConnectionMonitorImpl wrapModel(String name) {
        return new ConnectionMonitorImpl(name, parent, new ConnectionMonitorResultInner(), inner());
    }

    protected ConnectionMonitorImpl wrapModel(ConnectionMonitorResultInner inner) {
        return (inner == null) ? null : new ConnectionMonitorImpl(inner.name(), parent, inner, inner());
    }

    @Override
    public ConnectionMonitorImpl define(String name) {
        return new ConnectionMonitorImpl(name, parent, new ConnectionMonitorResultInner(), inner());
    }

    @Override
    public Mono<ConnectionMonitor> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name).map(inner -> wrapModel(inner));
    }

    @Override
    public ConnectionMonitor getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.inner().deleteAsync(parent.resourceGroupName(), parent.name(), name);
    }

    public ConnectionMonitorsClient inner() {
        return innerCollection;
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.inner().deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }
}
