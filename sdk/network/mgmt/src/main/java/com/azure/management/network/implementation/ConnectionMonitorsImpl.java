/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.ConnectionMonitor;
import com.azure.management.network.ConnectionMonitors;
import com.azure.management.network.models.ConnectionMonitorResultInner;
import com.azure.management.network.models.ConnectionMonitorsInner;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * Represents Connection Monitors collection associated with Network Watcher.
 */
class ConnectionMonitorsImpl extends
        CreatableResourcesImpl<ConnectionMonitor,
                ConnectionMonitorImpl,
                ConnectionMonitorResultInner>
        implements ConnectionMonitors {
    private final NetworkWatcherImpl parent;
    private final ConnectionMonitorsInner innerCollection;

    /**
     * Creates a new ConnectionMonitorsImpl.
     *
     * @param parent the Network Watcher associated with Connection Monitors
     */
    ConnectionMonitorsImpl(ConnectionMonitorsInner innerCollection, NetworkWatcherImpl parent) {
        this.parent = parent;
        this.innerCollection = innerCollection;
    }

    @Override
    public final PagedIterable<ConnectionMonitor> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /**
     * @return an observable emits connection monitors in this collection
     */
    @Override
    public PagedFlux<ConnectionMonitor> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected ConnectionMonitorImpl wrapModel(String name) {
        return new ConnectionMonitorImpl(name, parent, new ConnectionMonitorResultInner(), inner());
    }

    protected ConnectionMonitorImpl wrapModel(ConnectionMonitorResultInner inner) {
        return (inner == null) ? null : new ConnectionMonitorImpl(inner.getName(), parent, inner, inner());
    }

    @Override
    public ConnectionMonitorImpl define(String name) {
        return new ConnectionMonitorImpl(name, parent, new ConnectionMonitorResultInner(), inner());
    }

    @Override
    public Mono<ConnectionMonitor> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name)
                .map(inner -> wrapModel(inner));
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
        return this.inner().deleteAsync(parent.resourceGroupName(),
                parent.name(),
                name);
    }

    @Override
    public ConnectionMonitorsInner inner() {
        return innerCollection;
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.inner().deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }
}
