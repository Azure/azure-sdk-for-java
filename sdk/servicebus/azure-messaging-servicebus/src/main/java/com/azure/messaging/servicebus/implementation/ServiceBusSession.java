// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;


import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Represents an AMQP session that supports vendor specific properties and capabilities. For example, creating a
 * receiver that exclusively listens to a specific session.
 *
 * @see AmqpSession
 * @see ReactorSession
 */
public interface ServiceBusSession extends AmqpSession {
    /**
     * Creates a new AMQP link that consumes events from the message broker.
     *
     * @param linkName Name of the link.
     * @param entityPath The entity path this link connects to, so that it may read events from the message broker.
     * @param timeout Timeout required for creating and opening an AMQP link.
     * @param retryPolicy The retry policy to use when consuming messages.
     * @param receiveMode The {@link ServiceBusReceiveMode} for the messages to be received.
     *
     * @return A newly created AMQP link.
     */
    Mono<ServiceBusReceiveLink> createConsumer(String linkName, String entityPath, MessagingEntityType entityType,
        Duration timeout, AmqpRetryPolicy retryPolicy, ServiceBusReceiveMode receiveMode);

    /**
     * Creates a new AMQP link that consumes events from the message broker.
     *
     * @param linkName Name of the link.
     * @param entityPath The entity path this link connects to, so that it may read events from the message broker.
     * @param timeout Timeout required for creating and opening an AMQP link.
     * @param retryPolicy The retry policy to use when consuming messages.
     * @param receiveMode The {@link ServiceBusReceiveMode} for the messages to be received.
     * @param sessionId The sessionId for the messages to be received. If {@code null}, then the next, unnamed session
     *     is retrieved.
     *
     * @return A newly created AMQP link.
     */
    Mono<ServiceBusReceiveLink> createConsumer(String linkName, String entityPath, MessagingEntityType entityType,
        Duration timeout, AmqpRetryPolicy retryPolicy, ServiceBusReceiveMode receiveMode, String sessionId);

    /**
     * Creates a new {@link AmqpLink} that can send events to the message broker.
     *
     * @param linkName Name of the link.
     * @param entityPath The entity path this link connects to, so that it may send events to the message broker.
     * @param timeout Timeout required for creating and opening an AMQP link.
     * @param retryPolicy The retry policy to use when sending events.
     * @param transferEntityPath The entity path this link connects to, so that it may transfer events to
     *     the message broker via this entity.
     *
     * @return A newly created AMQP link.
     */
    Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retryPolicy, String transferEntityPath);
}
