// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Triple;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Event Hubs consumer group.
 */
public class EventHubConsumerGroupCrud
    extends AbstractResourceCrud<EventHubConsumerGroup, Triple<String, String, String>> {

    public EventHubConsumerGroupCrud(AzureResourceManager azureResourceManager,
                                     AzureResourceMetadata resourceMetadata) {
        super(azureResourceManager, resourceMetadata);
    }

    @Override
    String getResourceName(Triple<String, String, String> key) {
        return key.getThird();
    }

    @Override
    String getResourceType() {
        return EventHubConsumerGroup.class.getSimpleName();
    }

    @Override
    public EventHubConsumerGroup internalGet(Triple<String, String, String> consumerGroupCoordinate) {
        try {
            return this.resourceManager
                .eventHubs()
                .consumerGroups()
                .getByName(this.resourceMetadata.getResourceGroup(), consumerGroupCoordinate.getFirst(),
                           consumerGroupCoordinate.getSecond(), consumerGroupCoordinate.getThird());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHubConsumerGroup internalCreate(Triple<String, String, String> consumerGroupCoordinate) {
        return this.resourceManager
            .eventHubs()
            .consumerGroups()
            .define(consumerGroupCoordinate.getSecond())
            .withExistingEventHub(new EventHubCrud(this.resourceManager, this.resourceMetadata)
                                      .getOrCreate(Tuple.of(consumerGroupCoordinate.getFirst(),
                                                            consumerGroupCoordinate.getSecond())))
            .create();
    }
}
