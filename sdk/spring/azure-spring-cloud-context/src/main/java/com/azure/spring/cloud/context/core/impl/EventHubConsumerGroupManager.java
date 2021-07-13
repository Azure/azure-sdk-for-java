// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Event Hubs consumer group.
 */
public class EventHubConsumerGroupManager extends AzureManager<EventHubConsumerGroup, Tuple<EventHub, String>> {

    private final AzureResourceManager azureResourceManager;

    public EventHubConsumerGroupManager(AzureResourceManager azureResourceManager, AzureContextProperties azureContextProperties) {
        super(azureContextProperties);
        this.azureResourceManager = azureResourceManager;
    }

    @Override
    String getResourceName(Tuple<EventHub, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return EventHubConsumerGroup.class.getSimpleName();
    }

    @Override
    public EventHubConsumerGroup internalGet(Tuple<EventHub, String> eventHubAndGroup) {
        return eventHubAndGroup.getFirst()
                               .listConsumerGroups()
                               .stream()
                               .filter(c -> c.name().equals(eventHubAndGroup.getSecond()))
                               .findAny()
                               .orElse(null);
    }

    @Override
    public EventHubConsumerGroup internalCreate(Tuple<EventHub, String> eventHubAndGroup) {
        return azureResourceManager.eventHubs()
                                   .consumerGroups()
                                   .define(eventHubAndGroup.getSecond())
                                   .withExistingEventHub(eventHubAndGroup.getFirst())
                                   .create();
    }
}
