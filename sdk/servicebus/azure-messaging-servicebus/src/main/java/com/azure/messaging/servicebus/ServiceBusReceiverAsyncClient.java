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
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue or
 * topic on Azure Service Bus.
 *
 * <p><strong>Create an instance of receiver</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation}
 *
 * <p><strong>Create an instance of sender using default credential</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiateWithDefaultCredential}
 *
 * <p><strong>Receive all messages from Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#all }
 *
 * <p><strong>Receive messages in {@link ReceiveMode#RECEIVE_AND_DELETE} mode from Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receiveWithReceiveAndDeleteMode }
 *
 * <p><strong>Rate limiting consumption of messages from Service Bus resource</strong></p>
 * <p>For message receivers that need to limit the number of messages they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#basesubscriber}
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverClient To communicate with a Service Bus resource using a synchronous client.
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
        this.messageLockContainer = messageLockContainer;
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
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token. This will make the message available
     * again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken) {
        return abandon(lockToken, null);
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {
        return updateDisposition(lockToken, DispositionStatus.ABANDONED, null, null, propertiesToModify);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> complete(MessageLockToken lockToken) {
        return updateDisposition(lockToken, DispositionStatus.COMPLETED, null, null, null);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token. This will move message into the deferred
     * subqueue.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken) {
        return defer(lockToken, null);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {
        return updateDisposition(lockToken, DispositionStatus.DEFERRED, null, null, propertiesToModify);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken) {
        return deadLetter(lockToken, DEFAULT_DEAD_LETTER_OPTIONS);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} or {@code deadLetterOptions} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions) {
        if (Objects.isNull(deadLetterOptions)) {
            return monoError(logger, new NullPointerException("'deadLetterOptions' cannot be null."));
        }

        return updateDisposition(lockToken, DispositionStatus.SUSPENDED, deadLetterOptions.getDeadLetterReason(),
            deadLetterOptions.getDeadLetterErrorDescription(), deadLetterOptions.getPropertiesToModify());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peek() {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(ServiceBusManagementNode::peek);
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekAt(long sequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.peek(sequenceNumber));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage messages} that are peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages));
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekBatchAt(int maxMessages, long sequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages, sequenceNumber));
    }

    /**
     * Receives a stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     *
     * @return A stream of messages from the Service Bus entity.
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
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.receiveDeferredMessage(receiveMode, sequenceNumber));
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     *
     * @return A {@link Flux} of deferred {@link ServiceBusReceivedMessage messages}.
     */
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(long... sequenceNumbers) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.receiveDeferredMessageBatch(receiveMode, sequenceNumbers));
    }

    /**
     * Asynchronously renews the lock on the specified message. The lock will be renewed based on the setting specified
     * on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the Queue creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the
     * lock is reset to the entity's LockDuration value.
     *
     * @param lockToken Lock token of the message to renew.
     *
     * @return The new expiration time for the message.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Instant> renewMessageLock(MessageLockToken lockToken) {
        if (Objects.isNull(lockToken)) {
            return monoError(logger, new NullPointerException("'receivedMessage' cannot be null."));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(lockToken.getLockToken()))
            .map(instant -> {
                if (lockToken instanceof ServiceBusReceivedMessage) {
                    ((ServiceBusReceivedMessage) lockToken).setLockedUntil(instant);
                }

                return instant;
            });
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

    private Mono<Void> updateDisposition(MessageLockToken message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {

        if (Objects.isNull(message)) {
            return monoError(logger, new NullPointerException("'message' cannot be null."));
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
        logger.info("{}: Update started. Disposition: {}. Lock: {}. Expiration: {}",
            entityPath, dispositionStatus, lockToken, instant);

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
            logger.info("{}: Update completed. Disposition: {}. Lock: {}.", entityPath, dispositionStatus, lockToken);

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
        return new SessionErrorContext(getFullyQualifiedNamespace(), getEntityPath());
    }
}
