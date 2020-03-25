// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAsyncConsumer;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue or
 * topic on Azure Service Bus.
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverClient See ServiceBusReceiverClient to communicate with a Service Bus resource using a
 *     synchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements Closeable {
    private static final DeadLetterOptions DEFAULT_DEAD_LETTER_OPTIONS = new DeadLetterOptions();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final boolean isSessionEnabled;
    private final ReceiveMessageOptions receiveOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final int prefetch;
    private final ReceiveMode receiveMode;
    private final MessageLockContainer messageLockContainer;

    /**
     * Map containing linkNames and their associated consumers. Key: linkName Value: consumer associated with that
     * linkName.
     */
    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers = new ConcurrentHashMap<>();

    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        boolean isSessionEnabled, ReceiveMessageOptions receiveOptions,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, MessageLockContainer messageLockContainer) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.receiveOptions = Objects.requireNonNull(receiveOptions, "'receiveMessageOptions' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");

        this.prefetch = receiveOptions.getPrefetchCount();
        this.receiveMode = receiveOptions.getReceiveMode();

        this.entityType = entityType;
        this.isSessionEnabled = isSessionEnabled;
        this.messageLockContainer =  messageLockContainer;
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
     * Gets the Service Bus resource this client interacts with.
     *
     * @return The Service Bus resource this client interacts with.
     */
    public String getServiceBusResourceName() {
        return entityPath;
    }

    /**
     * Receives a stream of {@link ServiceBusReceivedMessage}.
     *
     * @return A stream of messages from Service Bus.
     */
    public Flux<ServiceBusReceivedMessage> receive() {
        if (isDisposed.get()) {
            return Flux.error(logger.logExceptionAsError(
                new IllegalStateException("Cannot receive from a client that is already closed.")));
        }

        if (receiveMode != ReceiveMode.PEEK_LOCK && receiveOptions.isAutoComplete()) {
            return Flux.error(logger.logExceptionAsError(new UnsupportedOperationException(
                "Autocomplete is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.")));
        }

        // TODO (conniey): This returns the same consumer instance because the entityPath is not unique.
        //  Python and .NET does not have the same behaviour.
        return Flux.usingWhen(
            Mono.fromCallable(() -> getOrCreateConsumer(entityPath)),
            consumer -> consumer.receive(),
            consumer -> {
                final String linkName = consumer.getLinkName();
                logger.info("{}: Receiving completed. Disposing", linkName);

                final ServiceBusAsyncConsumer removed = openConsumers.remove(linkName);
                if (removed == null) {
                    logger.warning("Could not find consumer to remove for: {}", linkName);
                } else {
                    removed.close();
                }

                return Mono.empty();
            });
    }

    /**
     * Abandon {@link ServiceBusMessage} with lock token. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message) {
        return abandon(message, null);
    }

    /**
     * Abandon {@link ServiceBusMessage} with lock token and updated message property. This will make the message
     * available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param message to be used.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message, Map<String, Object> propertiesToModify) {
        return updateDisposition(message, DispositionStatus.ABANDONED, null, null, propertiesToModify);
    }

    /**
     * Completes a {@link ServiceBusMessage} using its lock token. This will delete the message from the service.
     *
     * @param message Message to be completed.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.COMPLETED, null, null, null);
    }

    /**
     * Defers a {@link ServiceBusMessage} using its lock token. This will move message into deferred subqueue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message) {
        return defer(message, null);
    }

    /**
     * Asynchronously renews the lock on the specified message. The lock will be renewed based on the setting specified
     * on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the Queue creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the
     * lock is reset to the entity's LockDuration value.
     *
     * @param receivedMessage to be used to renew.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Instant> renewMessageLock(ServiceBusReceivedMessage receivedMessage) {
        Objects.requireNonNull(receivedMessage, "'receivedMessage' cannot be null.");

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(receivedMessage.getLockToken()))
            .map(instant -> {
                receivedMessage.setLockedUntil(instant);
                return instant;
            });
    }

    /**
     * Defers a {@link ServiceBusMessage} using its lock token with modified message property. This will move message
     * into deferred subqueue.
     *
     * @param message to be used.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message, Map<String, Object> propertiesToModify) {
        return updateDisposition(message, DispositionStatus.DEFERRED, null, null, propertiesToModify);
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
        return deadLetter(message, DEFAULT_DEAD_LETTER_OPTIONS);
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue with deadletter reason, error description and
     * modifided properties.
     *
     * @param message to be used.
     * @param deadLetterOptions The options to specify while moving message to the deadletter sub-queue.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, DeadLetterOptions deadLetterOptions) {
        Objects.requireNonNull(deadLetterOptions, "'deadLetterOptions' cannot be null.");

        return updateDisposition(message, DispositionStatus.SUSPENDED, deadLetterOptions.getDeadLetterReason(),
            deadLetterOptions.getDeadLetterErrorDescription(), deadLetterOptions.getPropertiesToModify());

    }

    /**
     * Receives a deferred {@link ServiceBusMessage}. Deferred messages can only be received by using sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber()}.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.receiveDeferredMessage(receiveMode, sequenceNumber));
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @return Single {@link ServiceBusReceivedMessage} peeked.
     */
    public Mono<ServiceBusReceivedMessage> peek() {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(ServiceBusManagementNode::peek);
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage}. Deferred messages can only be received by using sequence
     * number.
     *
     * @param sequenceNumbers of the messages to be received.
     *
     * @return The {@link Flux} of deferred {@link ServiceBusReceivedMessage}.
     */
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(long... sequenceNumbers) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.receiveDeferredMessageBatch(receiveMode, sequenceNumbers));
    }

    /**
     * Reads next the active message without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     *
     * @return Single {@link ServiceBusReceivedMessage} peeked.
     */
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.peek(fromSequenceNumber));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     *
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param fromSequenceNumber The sequence number from where to read the message.
     *
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, long fromSequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages, fromSequenceNumber));
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        logger.info("Removing receiver clients.");
        connectionProcessor.dispose();

        openConsumers.keySet().forEach(key -> {
            final ServiceBusAsyncConsumer consumer = openConsumers.get(key);
            if (consumer != null) {
                consumer.close();
            }
        });

        openConsumers.clear();
    }

    private Mono<Boolean> isLockTokenValid(UUID lockToken) {
        final Instant lockedUntilUtc = messageLockContainer.getLockTokenExpiration(lockToken);
        if (lockedUntilUtc == null) {
            logger.warning("lockToken[{}] is not owned by this receiver.", lockToken);
            return Mono.just(false);
        }

        //TODO (conniey): This check is done locally in Track 1. It's possible there is server skew.
        // who knows how reliable this is.
        final Instant now = Instant.now();
        if (lockedUntilUtc.isBefore(now)) {
            return Mono.error(logger.logExceptionAsError(new AmqpException(false, String.format(
                "Lock already expired for the lock token. Expiration: '%s'. Now: '%s'", lockedUntilUtc, now),
                getErrorContext())));
        }

        return Mono.just(true);
    }

    private Mono<Void> updateDisposition(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
        if (message == null) {
            return Mono.error(new NullPointerException("'message' cannot be null."));
        }

        final UUID lockToken = message.getLockToken();
        if (receiveMode != ReceiveMode.PEEK_LOCK) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (lockToken == null) {
            return Mono.error(logger.logExceptionAsError(new IllegalArgumentException(
                "'message.getLockToken()' cannot be null.")));
        }

        final Instant instant = messageLockContainer.getLockTokenExpiration(lockToken);
        logger.info("{}: Update started. Disposition: {}. Sequence number: {}. Lock: {}. Expiration: {}",
            entityPath, dispositionStatus, message.getSequenceNumber(), lockToken, instant);

        return isLockTokenValid(lockToken).flatMap(isLocked -> {
            return connectionProcessor.flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(node -> {
                    if (isLocked) {
                        return node.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                            deadLetterErrorDescription, propertiesToModify);
                    } else {
                        //TODO (conniey): in Track 1, I believe there was a way to do this.
                        return Mono.error(new UnsupportedOperationException(
                            "Cannot complete a message that is not locked. lockToken: " + lockToken));
                    }
                });
        }).then(Mono.fromRunnable(() -> {
            logger.info("{}: Update completed. Disposition: {}. Sequence number: {}. Lock: {}.",
                entityPath, dispositionStatus, message.getSequenceNumber(), lockToken);

            messageLockContainer.remove(lockToken);
        }));
    }

    private ServiceBusAsyncConsumer getOrCreateConsumer(String linkName) {
        return openConsumers.computeIfAbsent(linkName, name -> {
            logger.info("{}: Creating consumer for link '{}'", entityPath, linkName);

            final Flux<AmqpReceiveLink> receiveLink =
                connectionProcessor.flatMap(connection -> connection.createReceiveLink(linkName, entityPath,
                    receiveMode, isSessionEnabled, null, entityType))
                    .doOnNext(next -> {
                        final String format = "Created consumer for Service Bus resource: [{}] mode: [{}]"
                            + " sessionEnabled? {} transferEntityPath: [{}], entityType: [{}]";
                        logger.verbose(format, next.getEntityPath(), receiveMode, isSessionEnabled, "N/A",
                            entityType);
                    })
                    .repeat();

            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
            final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLink.subscribeWith(
                new ServiceBusReceiveLinkProcessor(prefetch, retryPolicy, connectionProcessor));

            return new ServiceBusAsyncConsumer(linkName, linkMessageProcessor, messageSerializer,
                receiveOptions.isAutoComplete(), receiveOptions.isLockAutoRenewed(),
                receiveOptions.getMaxAutoRenewDuration(), connectionProcessor.getRetryOptions(), messageLockContainer,
                this::complete, this::abandon, this::renewMessageLock);
        });
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getFullyQualifiedNamespace(), getServiceBusResourceName());
    }
}
