// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that manages the connection to Azure Event Hubs.
 */
class EventHubConnection implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubConnection.class);
    private final AtomicBoolean hasConnection = new AtomicBoolean();
    private final ConnectionOptions connectionOptions;
    private final Mono<EventHubAmqpConnection> currentConnection;

    /**
     * Creates a new instance of {@link EventHubConnection}.
     */
    EventHubConnection(Mono<EventHubAmqpConnection> createConnectionMono, ConnectionOptions connectionOptions) {
        this.connectionOptions = connectionOptions;
        this.currentConnection = createConnectionMono
            .doOnSubscribe(c -> hasConnection.set(true))
            .cache();
    }

    /**
     * Gets the fully qualified namespace for the connection.
     *
     * @return The fully qualified namespace this is connection.
     */
    public String getFullyQualifiedNamespace() {
        return connectionOptions.getFullyQualifiedNamespace();
    }

    /**
     * Gets the name of the Event Hub.
     *
     * @return The name of the Event Hub.
     */
    public String getEventHubName() {
        return connectionOptions.getEntityPath();
    }

    AmqpRetryOptions getRetryOptions() {
        return connectionOptions.getRetry();
    }

    /**
     * Gets the Event Hub management node.
     *
     * @return The Event Hub management node.
     */
    Mono<EventHubManagementNode> getManagementNode() {
        return currentConnection.flatMap(EventHubAmqpConnection::getManagementNode);
    }

    /**
     * Creates or gets a send link. The same link is returned if there is an existing send link with the same
     * {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param retryOptions Options to use when creating the link.
     *
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions) {
        return currentConnection.flatMap(connection -> connection.createSession(entityPath))
            .flatMap(session -> {
                logger.verbose("Creating producer for {}", entityPath);
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createProducer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy)
                    .cast(AmqpSendLink.class);
            });
    }

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with the
     * same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param eventPosition Position to set the receive link to.
     * @param options Consumer options to use when creating the link.
     *
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, EventPosition eventPosition,
        ReceiveOptions options) {
        return currentConnection.flatMap(connection -> connection.createSession(entityPath).cast(EventHubSession.class))
            .flatMap(session -> {
                logger.verbose("Creating consumer for path: {}", entityPath);
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());

                return session.createConsumer(linkName, entityPath, connectionOptions.getRetry().getTryTimeout(),
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
        if (!hasConnection.getAndSet(false)) {
            return;
        }

        final AmqpConnection connection = currentConnection.block(connectionOptions.getRetry().getTryTimeout());
        if (connection != null) {
            connection.close();
        }

        if (connectionOptions.getScheduler() != null && !connectionOptions.getScheduler().isDisposed()) {
            connectionOptions.getScheduler().dispose();
        }
    }
}
