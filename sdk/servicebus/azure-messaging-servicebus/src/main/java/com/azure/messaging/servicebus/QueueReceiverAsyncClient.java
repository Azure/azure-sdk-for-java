// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusAsyncConsumer;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Closeable;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.fluxError;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link Message} from a specific Queue.
 *
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueReceiverAsyncClient implements Closeable {

    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s";

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(QueueReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String queueName;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final MessageSerializer messageSerializer;
    private final int prefetchCount;
    private final TracerProvider tracerProvider;
    private final ReceiveMode defaultReceiveMode = ReceiveMode.PEEK_LOCK;

    /**
     * Consumer to maintain single connection per queue.
     */
    private final AtomicReference<ServiceBusAsyncConsumer> openConsumer =  new AtomicReference<>();

    QueueReceiverAsyncClient(String fullyQualifiedNamespace, String queueName,
                             ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
                             MessageSerializer messageSerializer, int prefetchCount) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.queueName = queueName;
        this.connectionProcessor = connectionProcessor;
        this.messageSerializer = messageSerializer;
        this.prefetchCount = prefetchCount;
        this.tracerProvider = tracerProvider;
    }

    /**
     * Gets the fully qualified Service Bus namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Queue name this client interacts with.
     *
     * @return The Queue name this client interacts with.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Receives a stream of {@link Message} with default server wait time.
     *
     * @return A stream of messages from Queue.
     */
    public Flux<Message> receive() {
        return receive(defaultReceiveMode);
    }

    /**
     * Consumes messages for given {@link ReceiveMode}.
     *
     * @param receiveMode {@link ReceiveMode} when receiving events from Queue.
     *
     * @return A stream of events for every partition from Queue.
     *
     * @throws NullPointerException if {@code receiveMode} is null.
     */
    public Flux<Message> receive(ReceiveMode receiveMode) {
        if (Objects.isNull(receiveMode)) {
            return fluxError(logger, new NullPointerException("'receiveMode' cannot be null."));
        }

        final String linkName = connectionProcessor.getEntityPath();
        return createConsumer(linkName, receiveMode);
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }
        if (openConsumer.get() != null) {
            openConsumer.get().close();
        }

        connectionProcessor.dispose();

    }

    private Flux<Message> createConsumer(String linkName, ReceiveMode receiveMode) {
        if (openConsumer.get() == null) {
            logger.info("{}: Creating receive consumer.", linkName);
            openConsumer.set(createServiceBusConsumer(linkName, receiveMode));
        }
        return openConsumer.get()
            .receive()
            .doOnCancel(() -> removeLink(linkName, SignalType.CANCEL))
            .doOnComplete(() -> removeLink(linkName, SignalType.ON_COMPLETE))
            .doOnError(error -> removeLink(linkName, SignalType.ON_ERROR));
    }

    private void removeLink(String linkName, SignalType signalType) {
        logger.info("{}: Receiving completed. Signal[{}]", linkName, signalType);

        if (openConsumer.get() != null) {
            openConsumer.get().close();
        }
    }
    private ServiceBusAsyncConsumer createServiceBusConsumer(String linkName, ReceiveMode receiveMode) {
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, getQueueName());

        final Flux<AmqpReceiveLink> receiveLinkMono =
            connectionProcessor.flatMap(connection ->
                connection.createReceiveLink(linkName, entityPath, receiveMode))
                .doOnNext(next -> logger.verbose("Creating consumer for path: {}", next.getEntityPath()))
                .repeat();

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLinkMono.subscribeWith(
            new ServiceBusReceiveLinkProcessor(prefetchCount, retryPolicy, connectionProcessor));

        return new ServiceBusAsyncConsumer(linkMessageProcessor, messageSerializer, fullyQualifiedNamespace,
            entityPath);
    }

    /**
     * Abandon {@link Message} with lock token and updated message property. This will make the message available
     * again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken to be used.
     * @param propertiesToModify Message properties to modify.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(UUID lockToken, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Abandon {@link Message} with lock token. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken to be used.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(UUID lockToken) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken to be used.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> complete(UUID lockToken) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     *  Defers a {@link Message} using its lock token with modified message property.
     *  This will move message into deferred subqueue.
     *
     * @param lockToken to be used.
     * @param propertiesToModify Message properties to modify.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(UUID lockToken, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Defers a {@link Message} using its lock token. This will move message into deferred subqueue.
     *
     * @param lockToken to be used.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(UUID lockToken) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link Message} to the deadletter sub-queue.
     *
     * @param lockToken to be used.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(UUID lockToken) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason, error description
     * and modifided properties.
     *
     * @param lockToken to be used.
     * @param deadLetterReason The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify Message properties to modify.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription,
                                 Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param lockToken to be used.
     * @param deadLetterReason The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link Message} to the deadletter sub-queue with modified message properties.
     * @param lockToken to be used.
     * @param propertiesToModify Message properties to modify.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during
     * the Queue creation (LockDuration). If processing of the message requires longer than this duration, the lock
     * needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param lockToken to be used.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Instant renewMessageLock(UUID lockToken) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Receives a deferred {@link Message}. Deferred messages can only be received by using sequence number.
     *
     * @param sequenceNumber The {@link Message#getSequenceNumber()}.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Message> receiveDeferredMessage(long sequenceNumber) {
        //TODO(feature-to-implement)
        return null;
    }

}
