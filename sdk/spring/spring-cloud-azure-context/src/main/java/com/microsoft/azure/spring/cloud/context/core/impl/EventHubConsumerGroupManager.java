/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;

public class EventHubConsumerGroupManager extends AzureManager<EventHubConsumerGroup, Tuple<EventHub, String>> {

    public EventHubConsumerGroupManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
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
        return eventHubAndGroup.getFirst().listConsumerGroups().stream()
                               .filter(c -> c.name().equals(eventHubAndGroup.getSecond())).findAny().orElse(null);
    }

    @Override
    public EventHubConsumerGroup internalCreate(Tuple<EventHub, String> eventHubAndGroup) {
        return azure.eventHubs().consumerGroups().define(eventHubAndGroup.getSecond())
                    .withExistingEventHub(eventHubAndGroup.getFirst()).create();
    }
}
