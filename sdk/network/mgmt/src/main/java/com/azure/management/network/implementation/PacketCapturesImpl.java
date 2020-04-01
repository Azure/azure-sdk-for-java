/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.network.PacketCapture;
import com.azure.management.network.PacketCaptures;
import com.azure.management.network.models.PacketCaptureResultInner;
import com.azure.management.network.models.PacketCapturesInner;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * Represents Packet Captures collection associated with Network Watcher.
 */
class PacketCapturesImpl extends
        CreatableResourcesImpl<PacketCapture,
                PacketCaptureImpl,
                PacketCaptureResultInner>
        implements PacketCaptures {
    private final NetworkWatcherImpl parent;
    protected final PacketCapturesInner innerCollection;

    /**
     * Creates a new PacketCapturesImpl.
     *
     * @param parent the Network Watcher associated with Packet Captures
     */
    PacketCapturesImpl(PacketCapturesInner innerCollection, NetworkWatcherImpl parent) {
        this.parent = parent;
        this.innerCollection = innerCollection;
    }

    @Override
    public final PagedIterable<PacketCapture> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /**
     * @return an observable emits packet captures in this collection
     */
    @Override
    public PagedFlux<PacketCapture> listAsync() {
        return wrapPageAsync(inner().listAsync(parent.resourceGroupName(), parent.name()));
    }

    @Override
    protected PacketCaptureImpl wrapModel(String name) {
        return new PacketCaptureImpl(name, parent, new PacketCaptureResultInner(), inner());
    }

    protected PacketCaptureImpl wrapModel(PacketCaptureResultInner inner) {
        return (inner == null) ? null : new PacketCaptureImpl(inner.name(), parent, inner, inner());
    }

    @Override
    public PacketCaptureImpl define(String name) {
        return new PacketCaptureImpl(name, parent, new PacketCaptureResultInner(), inner());
    }

    @Override
    public Mono<PacketCapture> getByNameAsync(String name) {
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name)
                .map(inner -> wrapModel(inner));
    }

    @Override
    public PacketCapture getByName(String name) {
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
    public PacketCapturesInner inner() {
        return innerCollection;
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.inner().deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }
}
