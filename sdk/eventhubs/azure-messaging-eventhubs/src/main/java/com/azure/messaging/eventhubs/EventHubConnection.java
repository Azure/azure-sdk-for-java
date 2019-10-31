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
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that manages the connection to Azure Event Hubs.
 */
public class EventHubConnection implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubConnection.class);
    private final AtomicBoolean hasConnection = new AtomicBoolean();
    private final ConnectionOptions connectionOptions;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final ReactorHandlerProvider handlerProvider;
    private final ReactorProvider provider;
    private final Mono<EventHubAmqpConnection> currentConnection;

    /**
     * Creates a new instance of {@link EventHubConnection}.
     */
    EventHubConnection(ConnectionOptions connectionOptions, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer) {
        this.connectionOptions = connectionOptions;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.provider = new ReactorProvider();
        this.handlerProvider = new ReactorHandlerProvider(provider);
        this.currentConnection = createConnection()
            .doOnSubscribe(c -> hasConnection.set(true))
            .cache();
    }

    String getFullyQualifiedDomainName() {
        return connectionOptions.getHostname();
    }

    String getEventHubName() {
        return connectionOptions.getEntityPath();
    }

    RetryOptions getRetryOptions() {
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
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, RetryOptions retryOptions) {
        return currentConnection.flatMap(connection -> connection.createSession(entityPath))
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
        return currentConnection.flatMap(connection -> connection.createSession(entityPath).cast(EventHubSession.class))
            .flatMap(session -> {
                logger.verbose("Creating consumer for path: {}", entityPath);
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());

                return session.createConsumer(linkName, entityPath, connectionOptions.getRetry().getTryTimeout(),
                    retryPolicy, eventPosition, options);
            });
    }

    private Mono<EventHubAmqpConnection> createConnection() {
        return Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");

            return new EventHubReactorAmqpConnection(connectionId, connectionOptions, provider, handlerProvider,
                tokenManagerProvider, messageSerializer);
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
                final AmqpConnection connection = currentConnection.block(connectionOptions.getRetry().getTryTimeout());
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException exception) {
                throw logger.logExceptionAsError(
                    new AmqpException(false, "Unable to close connection to service", exception,
                        new ErrorContext(connectionOptions.getHostname())));
            }
        }
    }
}
