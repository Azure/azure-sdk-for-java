// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Represents an AMQP session that supports vendor specific properties and capabilities. For example, creating a
 * receiver that exclusively listens to a partition + consumer group combination, or getting snapshots of partition
 * information.
 *
 * @see AmqpSession
 * @see ReactorSession
 */
public interface EventHubSession extends AmqpSession {
    /**
     * Creates a new AMQP consumer.
     *
     * @param linkName Name of the sender link.
     * @param entityPath The entity path this link connects to receive events.
     * @param timeout Timeout required for creating and opening AMQP link.
     * @param retry The retry policy to use when receiving messages.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options Options to use when creating the consumer.
     * @return A newly created AMQP link.
     */
    Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry,
                                         EventPosition eventPosition, ReceiveOptions options);
}
