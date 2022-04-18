// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;


/**
 * Resource manager for Azure Event Hubs consumer group.
 */
public class EventHubsConsumerGroupCrud
    extends AbstractResourceCrud<EventHubConsumerGroup, Tuple3<String, String, String>> {

    public EventHubsConsumerGroupCrud(AzureResourceManager azureResourceManager,
                                      AzureResourceMetadata resourceMetadata) {
        super(azureResourceManager, resourceMetadata);
    }

    @Override
    String getResourceName(Tuple3<String, String, String> key) {
        return key.getT3();
    }

    @Override
    String getResourceType() {
        return EventHubConsumerGroup.class.getSimpleName();
    }

    @Override
    public EventHubConsumerGroup internalGet(Tuple3<String, String, String> consumerGroupCoordinate) {
        try {
            return this.resourceManager
                .eventHubs()
                .consumerGroups()
                .getByName(this.resourceMetadata.getResourceGroup(), consumerGroupCoordinate.getT1(),
                           consumerGroupCoordinate.getT2(), consumerGroupCoordinate.getT3());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public EventHubConsumerGroup internalCreate(Tuple3<String, String, String> consumerGroupCoordinate) {
        return this.resourceManager
            .eventHubs()
            .consumerGroups()
            .define(consumerGroupCoordinate.getT3())
            .withExistingEventHub(new EventHubsCrud(this.resourceManager, this.resourceMetadata)
                                      .getOrCreate(Tuples.of(consumerGroupCoordinate.getT1(),
                                                            consumerGroupCoordinate.getT2())))
            .create();
    }
}
