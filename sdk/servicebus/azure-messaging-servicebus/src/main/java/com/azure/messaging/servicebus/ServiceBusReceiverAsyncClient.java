// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAsyncConsumer;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;

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
 * {@link BaseSubscriber#request(long)}.</p> {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#basesubscriber}
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverClient To communicate with a Service Bus resource using a synchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements AutoCloseable {
    private static final DeadLetterOptions DEFAULT_DEAD_LETTER_OPTIONS = new DeadLetterOptions();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final boolean isSessionEnabled;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final int prefetch;
    private final ReceiveMode receiveMode;
    private final MessageLockContainer messageLockContainer;
    private final ReceiveAsyncOptions defaultReceiveOptions;
    private final Runnable onClientClose;

    /**
     * Map containing linkNames and their associated consumers. Key: linkName Value: consumer associated with that
     * linkName.
     */
    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers = new ConcurrentHashMap<>();

    /**
     * Creates a receiver that listens to a Service Bus resource.
     *
     * @param fullyQualifiedNamespace The fully qualified domain name for the Service Bus resource.
     * @param entityPath The name of the topic or queue.
     * @param entityType The type of the Service Bus resource.
     * @param isSessionEnabled {@code true} if sessions are enabled; {@code false} otherwise.
     * @param receiverOptions Options when receiving messages.
     * @param connectionProcessor The AMQP connection to the Service Bus resource.
     * @param tracerProvider Tracer for telemetry.
     * @param messageSerializer Serializes and deserializes Service Bus messages.
     * @param messageLockContainer Container for message locks.
     * @param onClientClose Operation to run when the client completes.
     */
    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        boolean isSessionEnabled, ReceiverOptions receiverOptions,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, MessageLockContainer messageLockContainer, Runnable onClientClose) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveMessageOptions' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");

        this.prefetch = receiverOptions.getPrefetchCount();
        this.receiveMode = receiverOptions.getReceiveMode();

        this.entityType = entityType;
        this.isSessionEnabled = isSessionEnabled;
        this.messageLockContainer = messageLockContainer;
        this.onClientClose = onClientClose;

        this.defaultReceiveOptions = new ReceiveAsyncOptions()
            .setEnableAutoComplete(true)
            .setMaxAutoRenewDuration(connectionProcessor.getRetryOptions().getTryTimeout());
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
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peek")));
        }

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
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekAt")));
        }

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
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatch")));
        }

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
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatchAt")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages, sequenceNumber));
    }

    /**
     * Receives a stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity and completes them
     * when they are finished processing.
     *
     * <p>
     * By default, each successfully consumed message is {@link #complete(MessageLockToken) auto-completed} and {@link
     * #renewMessageLock(MessageLockToken) auto-renewed}. When downstream consumers throw an exception, the
     * auto-completion feature will {@link #abandon(MessageLockToken) abandon} the message. {@link
     * #renewMessageLock(MessageLockToken) Auto-renewal} occurs until the {@link AmqpRetryOptions#getTryTimeout()
     * operation timeout} has elapsed.
     * </p>
     *
     * @return A stream of messages from the Service Bus entity.
     * @throws AmqpException if {@link AmqpRetryOptions#getTryTimeout() operation timeout} has elapsed and
     *     downstream consumers are still processing the message.
     */
    public Flux<ServiceBusReceivedMessage> receive() {
        return receive(defaultReceiveOptions);
    }

    /**
     * Receives a stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity with a set of
     * options. To disable lock auto-renewal, set {@link ReceiveAsyncOptions#setMaxAutoRenewDuration(Duration)
     * setMaxAutoRenewDuration} to {@link Duration#ZERO} or {@code null}.
     *
     * @param options Set of options to set when receiving messages.
     * @return A stream of messages from the Service Bus entity.
     * @throws NullPointerException if {@code options} is null.
     * @throws IllegalArgumentException if {@link ReceiveAsyncOptions#getMaxAutoRenewDuration() max auto-renew
     *     duration} is negative.
     */
    public Flux<ServiceBusReceivedMessage> receive(ReceiveAsyncOptions options) {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receive")));
        }

        if (Objects.isNull(options)) {
            return fluxError(logger, new NullPointerException("'options' cannot be null"));
        } else if (options.getMaxAutoRenewDuration() != null && options.getMaxAutoRenewDuration().isNegative()) {
            return fluxError(logger, new IllegalArgumentException("'maxAutoRenewDuration' cannot be negative."));
        }

        if (receiveMode != ReceiveMode.PEEK_LOCK && options.isEnableAutoComplete()) {
            return Flux.error(logger.logExceptionAsError(new UnsupportedOperationException(
                "Auto-complete is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.")));
        }

        // TODO (conniey): This returns the same consumer instance because the entityPath is not unique.
        //  Python and .NET does not have the same behaviour.
        return Flux.usingWhen(
            Mono.fromCallable(() -> getOrCreateConsumer(entityPath, options)),
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
            .flatMap(node -> node.receiveDeferredMessage(receiveMode, sequenceNumber))
            .map(receivedMessage -> {
                if (receiveMode == ReceiveMode.PEEK_LOCK && !CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                    receivedMessage.setLockedUntil(messageLockContainer.addOrUpdate(receivedMessage.getLockToken(),
                        receivedMessage.getLockedUntil()));
                }
                return receivedMessage;
            });
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
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessageBatch")));
        }

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
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns an empty value.
     */
    public Mono<Instant> renewMessageLock(MessageLockToken lockToken) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewMessageLock")));
        } else if (Objects.isNull(lockToken)) {
            return monoError(logger, new NullPointerException("'receivedMessage' cannot be null."));
        } else if (Objects.isNull(lockToken.getLockToken())) {
            return monoError(logger, new NullPointerException("'receivedMessage.lockToken' cannot be null."));
        } else if (lockToken.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.lockToken' cannot be empty."));
        }

        UUID lockTokenUuid = null;
        try {
            lockTokenUuid = UUID.fromString(lockToken.getLockToken());
        } catch (IllegalArgumentException ex) {
            monoError(logger, ex);
        }

        UUID finalLockTokenUuid = lockTokenUuid;
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(finalLockTokenUuid))
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

        logger.info("Removing receiver links.");
        openConsumers.keySet().forEach(key -> {
            final ServiceBusAsyncConsumer consumer = openConsumers.get(key);
            if (consumer != null) {
                consumer.close();
            }
        });
        openConsumers.clear();

        onClientClose.run();
    }

    private Mono<Boolean> isLockTokenValid(String lockToken) {
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

        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, dispositionStatus.getValue())));
        } else if (Objects.isNull(message)) {
            return monoError(logger, new NullPointerException("'receivedMessage' cannot be null."));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(logger, new NullPointerException("'receivedMessage.lockToken' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.lockToken' cannot be empty."));
        }


        if (receiveMode != ReceiveMode.PEEK_LOCK) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(logger, new NullPointerException("'receivedMessage.lockToken' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.lockToken' cannot be empty."));
        }

        final String lockToken = message.getLockToken();
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

    private ServiceBusAsyncConsumer getOrCreateConsumer(String linkName, ReceiveAsyncOptions options) {
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

            final LinkErrorContext context = new LinkErrorContext(fullyQualifiedNamespace, entityPath, linkName, null);
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
            final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLink.subscribeWith(
                new ServiceBusReceiveLinkProcessor(prefetch, retryPolicy, connectionProcessor, context));
            final boolean isAutoLockRenewal = options.getMaxAutoRenewDuration() != null
                && !options.getMaxAutoRenewDuration().isZero();

            return new ServiceBusAsyncConsumer(linkName, linkMessageProcessor, messageSerializer,
                options.isEnableAutoComplete(), isAutoLockRenewal, options.getMaxAutoRenewDuration(),
                connectionProcessor.getRetryOptions(), messageLockContainer,
                this::complete, this::abandon, this::renewMessageLock);
        });
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getFullyQualifiedNamespace(), getEntityPath());
    }
}
