// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.fluent.PacketCapturesClient;
import com.azure.resourcemanager.network.fluent.models.PacketCaptureResultInner;
import com.azure.resourcemanager.network.models.PacketCapture;
import com.azure.resourcemanager.network.models.PacketCaptures;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableResourcesImpl;
import reactor.core.publisher.Mono;

/** Represents Packet Captures collection associated with Network Watcher. */
class PacketCapturesImpl extends CreatableResourcesImpl<PacketCapture, PacketCaptureImpl, PacketCaptureResultInner>
    implements PacketCaptures {
    private final NetworkWatcherImpl parent;
    protected final PacketCapturesClient innerCollection;

    /**
     * Creates a new PacketCapturesImpl.
     *
     * @param parent the Network Watcher associated with Packet Captures
     */
    PacketCapturesImpl(PacketCapturesClient innerCollection, NetworkWatcherImpl parent) {
        this.parent = parent;
        this.innerCollection = innerCollection;
    }

    @Override
    public final PagedIterable<PacketCapture> list() {
        return wrapList(inner().list(parent.resourceGroupName(), parent.name()));
    }

    /** @return an observable emits packet captures in this collection */
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
        return inner().getAsync(parent.resourceGroupName(), parent.name(), name).map(inner -> wrapModel(inner));
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
        return this.inner().deleteAsync(parent.resourceGroupName(), parent.name(), name);
    }

    public PacketCapturesClient inner() {
        return innerCollection;
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.inner().deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }
}
