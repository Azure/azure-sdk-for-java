// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.ConsumerGroupsClient;
import com.azure.resourcemanager.eventhubs.fluent.models.ConsumerGroupInner;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

import java.util.Objects;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for {@link EventHubConsumerGroups}.
 */
public final class EventHubConsumerGroupsImpl
    extends WrapperImpl<ConsumerGroupsClient>
    implements EventHubConsumerGroups {
    private final EventHubsManager manager;

    public EventHubConsumerGroupsImpl(EventHubsManager manager) {
        super(manager.serviceClient().getConsumerGroups());
        this.manager = manager;
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    @Override
    public EventHubConsumerGroupImpl define(String name) {
        return new EventHubConsumerGroupImpl(name, this.manager);
    }

    @Override
    public EventHubConsumerGroup getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<EventHubConsumerGroup> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<EventHubConsumerGroup> getByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.innerModel().getAsync(resourceGroupName, namespaceName, eventHubName, name).map(this::wrapModel);
    }

    @Override
    public EventHubConsumerGroup getByName(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, eventHubName, name).block();
    }

    @Override
    public PagedIterable<EventHubConsumerGroup> listByEventHub(
        String resourceGroupName, String namespaceName, String eventHubName) {
        return PagedConverter.mapPage(innerModel()
            .listByEventHub(resourceGroupName, namespaceName, eventHubName),
            this::wrapModel);
    }

    @Override
    public PagedFlux<EventHubConsumerGroup> listByEventHubAsync(
        String resourceGroupName, String namespaceName, String eventHubName) {
        return PagedConverter.mapPage(innerModel()
            .listByEventHubAsync(resourceGroupName, namespaceName, eventHubName),
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
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<Void> deleteByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.innerModel().deleteAsync(resourceGroupName,
                namespaceName,
                eventHubName,
                name);
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, eventHubName, name).block();
    }

    private EventHubConsumerGroupImpl wrapModel(ConsumerGroupInner innerModel) {
        return new EventHubConsumerGroupImpl(innerModel.name(), innerModel, this.manager);
    }
}
