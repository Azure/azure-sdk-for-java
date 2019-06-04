// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;

/**
 * An AMQP session representing bidirectional communication that supports multiple {@link AmqpLink}.
 */
public interface AmqpSession extends EndpointStateNotifier, Closeable {
    /**
     * Gets the entity path for this AMQP session.
     *
     * @return The entity path for the AMQP session.
     */
    String getSessionName();

    /**
     * Gets the operation timeout for starting the AMQP session.
     *
     * @return The timeout for starting the AMQP session.
     */
    Duration getOpenTimeout();

    /**
     * Creates a new AMQP sender link.
     *
     * @param linkName Name of the link.
     * @param timeout Timeout required for creating and opening AMPQ link.
     * @return A newly created AMQP link.
     */
    Mono<AmqpLink> createSender(String linkName, Duration timeout);

    /**
     * Creates a new AMQP receiver link.
     *
     * @param linkName Name of the link.
     * @param timeout Timeout required for creating and opening AMPQ link.
     * @return A newly created AMQP link.
     */
    Mono<AmqpLink> createReceiver(String linkName, Duration timeout);

    /**
     * Removes a {@link AmqpLink} with the given {@code linkName}.
     *
     * @param linkName Name of the link to remove.
     * @return {@code true} if the link was removed; {@code false} otherwise.
     */
    boolean removeLink(String linkName);
}
