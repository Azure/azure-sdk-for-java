// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.EventHubsClient;
import com.azure.resourcemanager.eventhubs.fluent.models.EventhubInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroups;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import reactor.core.publisher.Mono;

import java.util.Objects;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for {@link EventHubs}.
 */
public final class EventHubsImpl extends WrapperImpl<EventHubsClient> implements EventHubs {
    private final EventHubsManager manager;
    private final StorageManager storageManager;

    public EventHubsImpl(EventHubsManager manager, StorageManager storageManager) {
        super(manager.serviceClient().getEventHubs());
        this.manager = manager;
        this.storageManager = storageManager;
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    @Override
    public EventHubImpl define(String name) {
        return new EventHubImpl(name, this.manager, this.storageManager);
    }

    public EventHubAuthorizationRules authorizationRules() {
        return this.manager().eventHubAuthorizationRules();
    }

    public EventHubConsumerGroups consumerGroups() {
        return this.manager().consumerGroups();
    }

    @Override
    public EventHub getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<EventHub> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<EventHub> getByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.innerModel().getAsync(resourceGroupName,
            namespaceName,
            name)
            .map(this::wrapModel);
    }

    @Override
    public EventHub getByName(String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    @Override
    public PagedIterable<EventHub> listByNamespace(String resourceGroupName, String namespaceName) {
        return PagedConverter.mapPage(innerModel()
            .listByNamespace(resourceGroupName, namespaceName),
            this::wrapModel);
    }

    @Override
    public PagedFlux<EventHub> listByNamespaceAsync(String resourceGroupName, String namespaceName) {
        return PagedConverter.mapPage(innerModel()
            .listByNamespaceAsync(resourceGroupName, namespaceName),
            this::wrapModel);
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return deleteByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<Void> deleteByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.innerModel().deleteAsync(resourceGroupName,
            namespaceName,
            name);
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    private EventHubImpl wrapModel(EventhubInner innerModel) {
        return new EventHubImpl(innerModel.name(), innerModel, this.manager, this.storageManager);
    }
}
