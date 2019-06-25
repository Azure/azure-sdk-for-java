// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.messaging.eventhubs.PartitionProperties;

import java.util.Map;

/**
 * Mapper to help deserialize an AMQP message.
 */
public interface AmqpResponseMapper {
    /**
     * Deserialize the AMQP body to {@link EventHubProperties}.
     *
     * @param amqpBody AMQP response body to deserialize.
     * @return The {@link EventHubProperties} represented by the AMQP body.
     */
    EventHubProperties toEventHubProperties(Map<?, ?> amqpBody);

    /**
     * Deserialize the AMQP body to {@link PartitionProperties}.
     *
     * @param amqpBody AMQP response body to deserialize.
     * @return The {@link PartitionProperties} represented by the AMQP body.
     */
    PartitionProperties toPartitionProperties(Map<?, ?> amqpBody);
}
