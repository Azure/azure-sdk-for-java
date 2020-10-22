// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * The builder that creates session-related {@link ServiceBusReceiverAsyncClient} instances.
 * Use {@link #acceptSession(String)} to create a {@link ServiceBusReceiverAsyncClient} that is tied to a known specific
 * session id.
 * Use {@link #acceptNextSession()} to create a {@link ServiceBusReceiverAsyncClient} that is tied to an unknown
 * available session.
 * Use {@link #getReceiverClient(int)} to create a {@link ServiceBusReceiverAsyncClient} that
 * process events from up to maxConcurrentSessions number of sessions.
 */
public class ServiceBusSessionReceiverAsyncClient implements AutoCloseable {
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final ServiceBusSessionManager unNamedSessionManager;  // for acceptNextSession()
    private final ClientLogger logger = new ClientLogger(ServiceBusSessionReceiverAsyncClient.class);

    ServiceBusSessionReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath,
        MessagingEntityType entityType, ReceiverOptions receiverOptions,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, Runnable onClientClose) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.unNamedSessionManager = new ServiceBusSessionManager(entityPath, entityType,
            connectionProcessor, tracerProvider,
            messageSerializer, receiverOptions);
    }

    /**
     * Create a link for the next available session and use the link to create a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from that session.
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the available session.
     */
    public Mono<ServiceBusReceiverAsyncClient> acceptNextSession() {
        return unNamedSessionManager.getActiveLink().flatMap(receiveLink -> receiveLink.getSessionId()
            .map(sessionId -> {
                ReceiverOptions newReceiverOptions = new ReceiverOptions(receiverOptions.getReceiveMode(),
                    receiverOptions.getPrefetchCount(), sessionId, null, receiverOptions.getMaxLockRenewDuration());
                final ServiceBusSessionManager sessionSpecificManager = new ServiceBusSessionManager(entityPath,
                    entityType, connectionProcessor, tracerProvider, messageSerializer, newReceiverOptions,
                    receiveLink);
                return new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath,
                    entityType, newReceiverOptions, connectionProcessor, ServiceBusConstants.OPERATION_TIMEOUT,
                    tracerProvider, messageSerializer, onClientClose, sessionSpecificManager);
            }));
    }

    /**
     * Create a link for the "sessionId" and use the link to create a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from the session.
     * @param sessionId The session Id.
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the specified session.
     * @throws IllegalArgumentException if {@code sessionId} is null or empty.
     */
    public Mono<ServiceBusReceiverAsyncClient> acceptSession(String sessionId) {
        if (CoreUtils.isNullOrEmpty(sessionId)) {
            return monoError(logger, new IllegalArgumentException("sessionId can not be null or empty"));
        }
        ReceiverOptions newReceiverOptions = new ReceiverOptions(receiverOptions.getReceiveMode(),
            receiverOptions.getPrefetchCount(), sessionId, null, receiverOptions.getMaxLockRenewDuration());
        final ServiceBusSessionManager sessionSpecificManager = new ServiceBusSessionManager(entityPath, entityType,
            connectionProcessor, tracerProvider, messageSerializer, newReceiverOptions);

        return sessionSpecificManager.getActiveLink().thenReturn(new ServiceBusReceiverAsyncClient(
            fullyQualifiedNamespace, entityPath, entityType, newReceiverOptions, connectionProcessor,
            ServiceBusConstants.OPERATION_TIMEOUT, tracerProvider, messageSerializer, onClientClose,
            sessionSpecificManager));
    }

    /**
     * Create a {@link ServiceBusReceiverAsyncClient} that processes at most {@code maxConcurrentSessions} sessions.
     *
     * @param maxConcurrentSessions Maximum number of concurrent sessions to process at any given time.
     *
     * @return The {@link ServiceBusReceiverAsyncClient} object that will be used to receive messages.
     * @throws IllegalArgumentException if {@code maxConcurrentSessions} is less than 1.
     */
    public ServiceBusReceiverAsyncClient getReceiverClient(int maxConcurrentSessions) {
        if (maxConcurrentSessions < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Maximum number of concurrent sessions must be positive."));
        }
        ReceiverOptions newReceiverOptions = new ReceiverOptions(this.receiverOptions.getReceiveMode(),
            this.receiverOptions.getPrefetchCount(), null, maxConcurrentSessions,
            this.receiverOptions.getMaxLockRenewDuration());
        return new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath,
            entityType, newReceiverOptions, connectionProcessor, ServiceBusConstants.OPERATION_TIMEOUT,
            tracerProvider, messageSerializer, onClientClose);
    }

    @Override
    public void close() {
        this.onClientClose.run();
    }
}
