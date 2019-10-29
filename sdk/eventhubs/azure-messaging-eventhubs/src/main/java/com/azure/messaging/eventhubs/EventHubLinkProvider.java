// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.RetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Package-private class that manages the creation of AMQP links.
 */
class EventHubLinkProvider implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubLinkProvider.class);
    private final Mono<EventHubAmqpConnection> connectionMono;
    private final String hostname;
    private final RetryOptions retryOptions;
    private final AtomicBoolean hasConnection = new AtomicBoolean();

    /**
     * Creates a new instance of {@link EventHubLinkProvider}.
     *
     * @param connection A connection to the Event Hub.
     * @param hostname The FQDN of the Event Hub.
     * @param retryOptions Retry options to use when creating the link.
     * @throws NullPointerException if {@code connection}, {@code hostname}, or {@code retryOptions} is null.
     */
    EventHubLinkProvider(Mono<EventHubAmqpConnection> connection, String hostname, RetryOptions retryOptions) {
        this.connectionMono = Objects.requireNonNull(connection, "'connection' cannot be null.")
            .doOnSubscribe(c -> hasConnection.set(true))
            .cache();
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
    }

    /**
     * Gets the Event Hub management node.
     *
     * @return The Event Hub management node.
     */
    Mono<EventHubManagementNode> getManagementNode() {
        return connectionMono.flatMap(EventHubAmqpConnection::getManagementNode);
    }

    /**
     * Creates or gets a send link. The same link is returned if there is an existing send link with the same
     * {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param retryOptions Options to use when creating the link.
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, RetryOptions retryOptions) {
        return connectionMono.flatMap(connection -> connection.createSession(entityPath))
            .flatMap(session -> {
                logger.verbose("Creating producer for {}", entityPath);
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createProducer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy)
                    .cast(AmqpSendLink.class);
            });
    }

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with
     * the same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param eventPosition Position to set the receive link to.
     * @param options Consumer options to use when creating the link.
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, EventPosition eventPosition,
            EventHubConsumerOptions options) {
        return connectionMono.flatMap(connection -> connection.createSession(entityPath).cast(EventHubSession.class))
            .flatMap(session -> {
                logger.verbose("Creating consumer for path: {}", entityPath);
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(options.getRetry());

                return session.createConsumer(linkName, entityPath, options.getRetry().getTryTimeout(),
                    retryPolicy, eventPosition, options);
            });
    }

    /**
     * Disposes of the Event Hub connection.
     *
     * @throws AmqpException if the connection encountered an exception while closing.
     */
    @Override
    public void close() {
        if (hasConnection.getAndSet(false)) {
            try {
                final AmqpConnection connection = connectionMono.block(retryOptions.getTryTimeout());
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException exception) {
                throw logger.logExceptionAsError(
                    new AmqpException(false, "Unable to close connection to service", exception,
                        new ErrorContext(hostname)));
            }
        }
    }
}
