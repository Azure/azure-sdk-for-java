// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage messages} from a specific
 * queue or topic subscription.
 *
 * <p><strong>Create an instance of receiver</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation}
 *
 * <p><strong>Create an instance of receiver using default credential</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiateWithDefaultCredential}
 *
 * <p><strong>Receive all messages from Service Bus resource</strong></p>
 * <p>This returns an infinite stream of messages from Service Bus. The stream ends when the subscription is disposed
 * or other terminal scenarios. See {@link #receiveMessages()} for more information.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#all}
 *
 * <p><strong>Receive messages in {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode from a Service Bus
 * entity</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode}
 *
 * <p><strong>Receive messages from a specific session</strong></p>
 * <p>To fetch messages from a specific session, switch to {@link ServiceBusSessionReceiverClientBuilder} and
 * build the session receiver client. Use {@link ServiceBusSessionReceiverAsyncClient#acceptSession(String)} to create
 * a session-bound {@link ServiceBusReceiverAsyncClient}.
 * </p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId}
 *
 * <p><strong>Receive messages from the first available session</strong></p>
 * <p>To process messages from the first available session, switch to {@link ServiceBusSessionReceiverClientBuilder}
 * and build the session receiver client. Use
 * {@link ServiceBusSessionReceiverAsyncClient#acceptNextSession() acceptNextSession()} to find the first available
 * session to process messages from.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession}
 *
 * <p><strong>Rate limiting consumption of messages from a Service Bus entity</strong></p>
 * <p>For message receivers that need to limit the number of messages they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber}
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverClient To communicate with a Service Bus resource using a synchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements AutoCloseable {
    private static final DeadLetterOptions DEFAULT_DEAD_LETTER_OPTIONS = new DeadLetterOptions();
    private static final String TRANSACTION_LINK_NAME = "coordinator";

    private final LockContainer<LockRenewalOperation> renewalContainer;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final LockContainer<OffsetDateTime> managementNodeLocks;
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final ServiceBusSessionManager sessionManager;
    private final Semaphore completionLock = new Semaphore(1);

    // Starting at -1 because that is before the beginning of the stream.
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong(-1);
    private final AtomicReference<ServiceBusAsyncConsumer> consumer = new AtomicReference<>();

    /**
     * Creates a receiver that listens to a Service Bus resource.
     *
     * @param fullyQualifiedNamespace The fully qualified domain name for the Service Bus resource.
     * @param entityPath The name of the topic or queue.
     * @param entityType The type of the Service Bus resource.
     * @param receiverOptions Options when receiving messages.
     * @param connectionProcessor The AMQP connection to the Service Bus resource.
     * @param tracerProvider Tracer for telemetry.
     * @param messageSerializer Serializes and deserializes Service Bus messages.
     * @param onClientClose Operation to run when the client completes.
     */
    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        ReceiverOptions receiverOptions, ServiceBusConnectionProcessor connectionProcessor, Duration cleanupInterval,
        TracerProvider tracerProvider, MessageSerializer messageSerializer, Runnable onClientClose) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");

        this.managementNodeLocks = new LockContainer<>(cleanupInterval);
        this.renewalContainer = new LockContainer<>(Duration.ofMinutes(2), renewal -> {
            logger.verbose("Closing expired renewal operation. lockToken[{}]. status[{}]. throwable[{}].",
                renewal.getLockToken(), renewal.getStatus(), renewal.getThrowable());
            renewal.close();
        });

        this.sessionManager = null;
    }

    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        ReceiverOptions receiverOptions, ServiceBusConnectionProcessor connectionProcessor, Duration cleanupInterval,
        TracerProvider tracerProvider, MessageSerializer messageSerializer, Runnable onClientClose,
        ServiceBusSessionManager sessionManager) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.sessionManager = Objects.requireNonNull(sessionManager, "'sessionManager' cannot be null.");

        this.managementNodeLocks = new LockContainer<>(cleanupInterval);
        this.renewalContainer = new LockContainer<>(Duration.ofMinutes(2), renewal -> {
            logger.info("Closing expired renewal operation. sessionId[{}]. status[{}]. throwable[{}]",
                renewal.getSessionId(), renewal.getStatus(), renewal.getThrowable());
            renewal.close();
        });
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
     * Abandons a {@link ServiceBusReceivedMessage message}. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be abandoned.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.ABANDONED, null, null,
            null, null);
    }

    /**
     * Abandons a {@link ServiceBusReceivedMessage message} updates the message's properties. This will make the
     * message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options The options to set while abandoning the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be abandoned.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message, AbandonOptions options) {
        if (Objects.isNull(options)) {
            return monoError(logger, new NullPointerException("'settlementOptions' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(logger, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.ABANDONED, null, null,
            options.getPropertiesToModify(), options.getTransactionContext());
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message}. This will delete the message from the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be completed.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
            null, null);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} with the given options. This will delete the message from
     * the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to complete the message.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be completed.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message, CompleteOptions options) {
        if (Objects.isNull(options)) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(logger, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
            null, options.getTransactionContext());
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message}. This will move message into the deferred sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the Service Bus defer operation finishes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.DEFERRED, null, null,
            null, null);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} with the options set. This will move message into
     * the deferred sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to defer the message.
     *
     * @return A {@link Mono} that completes when the defer operation finishes.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message, DeferOptions options) {
        if (Objects.isNull(options)) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(logger, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.DEFERRED, null, null,
            options.getPropertiesToModify(), options.getTransactionContext());
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be dead-lettered.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
        return deadLetter(message, DEFAULT_DEAD_LETTER_OPTIONS);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue with the given options.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to dead-letter the message.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be dead-lettered.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, DeadLetterOptions options) {
        if (Objects.isNull(options)) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(logger, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(message, DispositionStatus.SUSPENDED, options.getDeadLetterReason(),
            options.getDeadLetterErrorDescription(), options.getPropertiesToModify(),
            options.getTransactionContext());
    }

    /**
     * Gets the state of the session if this receiver is a session receiver.
     *
     * @return The session state or an empty Mono if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already closed.
     * @throws ServiceBusException if the session state could not be acquired.
     */
    public Mono<byte[]> getSessionState() {
        return getSessionState(receiverOptions.getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekMessage() {
        return peekMessage(receiverOptions.getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if the receiver is disposed.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Mono<ServiceBusReceivedMessage> peekMessage(String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peek")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(channel -> {
                final long sequence = lastPeekedSequenceNumber.get() + 1;

                logger.verbose("Peek message from sequence number: {}", sequence);
                return channel.peek(sequence, sessionId, getLinkName(sessionId));
            })
            .handle((message, sink) -> {
                final long current = lastPeekedSequenceNumber
                    .updateAndGet(value -> Math.max(value, message.getSequenceNumber()));

                logger.verbose("Updating last peeked sequence number: {}", current);
                sink.next(message);
            });
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekMessage(long sequenceNumber) {
        return peekMessage(sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Mono<ServiceBusReceivedMessage> peekMessage(long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekAt")));
        }
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.peek(sequenceNumber, sessionId, getLinkName(sessionId)));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage messages} that are peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages) {
        return peekMessages(maxMessages, receiverOptions.getSessionId());
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} that are peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatch")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> {
                final long nextSequenceNumber = lastPeekedSequenceNumber.get() + 1;
                logger.verbose("Peek batch from sequence number: {}", nextSequenceNumber);

                final Flux<ServiceBusReceivedMessage> messages =
                    node.peek(nextSequenceNumber, sessionId, getLinkName(sessionId), maxMessages);

                // To prevent it from throwing NoSuchElementException in .last(), we produce an empty message with
                // the same sequence number.
                final Mono<ServiceBusReceivedMessage> handle = messages
                    .switchIfEmpty(Mono.fromCallable(() -> {
                        ServiceBusReceivedMessage emptyMessage = new ServiceBusReceivedMessage(BinaryData
                            .fromBytes(new byte[0]));
                        emptyMessage.setSequenceNumber(lastPeekedSequenceNumber.get());
                        return emptyMessage;
                    }))
                    .last()
                    .handle((last, sink) -> {
                        final long current = lastPeekedSequenceNumber
                            .updateAndGet(value -> Math.max(value, last.getSequenceNumber()));

                        logger.verbose("Last peeked sequence number in batch: {}", current);
                        sink.complete();
                    });

                return Flux.merge(messages, handle);
            });
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage messages} peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber) {
        return peekMessages(maxMessages, sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatchAt")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peek(sequenceNumber, sessionId, getLinkName(sessionId), maxMessages));
    }

    /**
     * Receives an <b>infinite</b> stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     * This Flux continuously receives messages from a Service Bus entity until either:
     *
     * <ul>
     *     <li>The receiver is closed.</li>
     *     <li>The subscription to the Flux is disposed.</li>
     *     <li>A terminal signal from a downstream subscriber is propagated upstream (ie. {@link Flux#take(long)} or
     *     {@link Flux#take(Duration)}).</li>
     *     <li>An {@link AmqpException} occurs that causes the receive link to stop.</li>
     * </ul>
     *
     * @return An <b>infinite</b> stream of messages from the Service Bus entity.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while receiving messages.
     */
    public Flux<ServiceBusReceivedMessage> receiveMessages() {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveMessages")));
        }
        // Without limitRate(), if the user calls receiveMessages().subscribe(), it will call
        // ServiceBusReceiveLinkProcessor.request(long request) where request = Long.MAX_VALUE.
        // We turn this one-time non-backpressure request to continuous requests with backpressure.
        // If receiverOptions.prefetchCount is set to non-zero, it will be passed to ServiceBusReceiveLinkProcessor
        // to auto-refill the prefetch buffer. A request will retrieve one message from this buffer.
        // If receiverOptions.prefetchCount is 0 (default value),
        // the request will add a link credit so one message is retrieved from the service.
        return receiveMessagesNoBackPressure().limitRate(1, 0);
    }

    Flux<ServiceBusReceivedMessage> receiveMessagesNoBackPressure() {
        return receiveMessagesWithContext(0)
            .handle((serviceBusMessageContext, sink) -> {
                if (serviceBusMessageContext.hasError()) {
                    sink.error(serviceBusMessageContext.getThrowable());
                    return;
                }
                sink.next(serviceBusMessageContext.getMessage());
            });
    }

    /**
     * Receives an <b>infinite</b> stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     * This Flux continuously receives messages from a Service Bus entity until either:
     *
     * <ul>
     *     <li>The receiver is closed.</li>
     *     <li>The subscription to the Flux is disposed.</li>
     *     <li>A terminal signal from a downstream subscriber is propagated upstream (ie. {@link Flux#take(long)} or
     *     {@link Flux#take(Duration)}).</li>
     *     <li>An {@link AmqpException} occurs that causes the receive link to stop.</li>
     * </ul>
     *
     * @return An <b>infinite</b> stream of messages from the Service Bus entity.
     */
    Flux<ServiceBusMessageContext> receiveMessagesWithContext() {
        return receiveMessagesWithContext(1);
    }

    Flux<ServiceBusMessageContext> receiveMessagesWithContext(int highTide) {
        final Flux<ServiceBusMessageContext> messageFlux = sessionManager != null
            ? sessionManager.receive()
            : getOrCreateConsumer().receive().map(ServiceBusMessageContext::new);

        final Flux<ServiceBusMessageContext> withAutoLockRenewal;
        if (!receiverOptions.isSessionReceiver() && receiverOptions.isAutoLockRenewEnabled()) {
            withAutoLockRenewal = new FluxAutoLockRenew(messageFlux, receiverOptions,
                renewalContainer, this::renewMessageLock);
        } else {
            withAutoLockRenewal = messageFlux;
        }

        Flux<ServiceBusMessageContext> result;
        if (receiverOptions.isEnableAutoComplete()) {
            result = new FluxAutoComplete(withAutoLockRenewal, completionLock,
                context -> context.getMessage() != null ? complete(context.getMessage()) : Mono.empty(),
                context -> context.getMessage() != null ? abandon(context.getMessage()) : Mono.empty());
        } else {
            result = withAutoLockRenewal;
        }

        if (highTide > 0) {
            result = result.limitRate(highTide, 0);
        }
        return result
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred message cannot be received.
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessage")));
        }
        return receiveDeferredMessage(sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     * @param sessionId Session id of the deferred message. {@code null} if there is no session.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     * @throws IllegalStateException if receiver is already disposed.
     */
    Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessage")));
        }
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.receiveDeferredMessages(receiverOptions.getReceiveMode(),
                sessionId, getLinkName(sessionId), Collections.singleton(sequenceNumber)).last())
            .map(receivedMessage -> {
                if (CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                    return receivedMessage;
                }
                if (receiverOptions.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK) {
                    receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
                        receivedMessage.getLockedUntil(),
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
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred messages cannot be received.
     */
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessages(Iterable<Long> sequenceNumbers) {
        return receiveDeferredMessages(sequenceNumbers, receiverOptions.getSessionId());
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     * @param sessionId Session id of the deferred messages. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of deferred {@link ServiceBusReceivedMessage messages}.
     * @throws IllegalStateException if receiver is already disposed.
     */
    Flux<ServiceBusReceivedMessage> receiveDeferredMessages(Iterable<Long> sequenceNumbers, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessageBatch")));
        }
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.receiveDeferredMessages(receiverOptions.getReceiveMode(),
                sessionId, getLinkName(sessionId), sequenceNumbers))
            .map(receivedMessage -> {
                if (CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                    return receivedMessage;
                }
                if (receiverOptions.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK) {
                    receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
                        receivedMessage.getLockedUntil(),
                        receivedMessage.getLockedUntil()));
                }

                return receivedMessage;
            });
    }

    /**
     * Asynchronously renews the lock on the message. The lock will be renewed based on the setting specified on the
     * entity. When a message is received in {@link ServiceBusReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the entity creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the
     * lock is reset to the entity's LockDuration value.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform auto-lock renewal.
     *
     * @return The new expiration time for the message.
     *
     * @throws NullPointerException if {@code message} or {@code message.getLockToken()} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from peekMessage.
     * @throws IllegalStateException if the receiver is a session receiver or receiver is already disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     */
    public Mono<OffsetDateTime> renewMessageLock(ServiceBusReceivedMessage message) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewMessageLock")));
        } else if (Objects.isNull(message)) {
            return monoError(logger, new NullPointerException("'message' cannot be null."));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(logger, new NullPointerException("'message.getLockToken()' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.getLockToken()' cannot be empty."));
        } else if (receiverOptions.isSessionReceiver()) {
            final String errorMessage = "Renewing message lock is an invalid operation when working with sessions.";
            return monoError(logger, new IllegalStateException(errorMessage));
        }

        return renewMessageLock(message.getLockToken())
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    /**
     * Asynchronously renews the lock on the message. The lock will be renewed based on the setting specified on the
     * entity.
     *
     * @param lockToken to be renewed.
     *
     * @return The new expiration time for the message.
     * @throws IllegalStateException if receiver is already disposed.
     */
    Mono<OffsetDateTime> renewMessageLock(String lockToken) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewMessageLock")));
        }
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(lockToken, getLinkName(null)))
            .map(offsetDateTime -> managementNodeLocks.addOrUpdate(lockToken, offsetDateTime,
                offsetDateTime));
    }

    /**
     * Starts the auto lock renewal for a {@link ServiceBusReceivedMessage message}.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param maxLockRenewalDuration Maximum duration to keep renewing the lock token.
     *
     * @return A Mono that completes when the message renewal operation has completed up until
     *      {@code maxLockRenewalDuration}.
     *
     * @throws NullPointerException if {@code message}, {@code message.getLockToken()}, or
     *      {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a session receiver or the receiver is disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     * @throws ServiceBusException If the message lock cannot be renewed.
     */
    public Mono<Void> renewMessageLock(ServiceBusReceivedMessage message, Duration maxLockRenewalDuration) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getAutoRenewMessageLock")));
        } else if (Objects.isNull(message)) {
            return monoError(logger, new NullPointerException("'message' cannot be null."));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(logger, new NullPointerException("'message.getLockToken()' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.getLockToken()' cannot be empty."));
        } else if (receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException(
                String.format("Cannot renew message lock [%s] for a session receiver.", message.getLockToken())));
        } else if (maxLockRenewalDuration == null) {
            return monoError(logger, new NullPointerException("'maxLockRenewalDuration' cannot be null."));
        } else if (maxLockRenewalDuration.isNegative()) {
            return monoError(logger, new IllegalArgumentException("'maxLockRenewalDuration' cannot be negative."));
        }

        final LockRenewalOperation operation = new LockRenewalOperation(message.getLockToken(),
            maxLockRenewalDuration, false, ignored -> renewMessageLock(message));
        renewalContainer.addOrUpdate(message.getLockToken(), OffsetDateTime.now().plus(maxLockRenewalDuration),
            operation);

        return operation.getCompletionOperation()
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    /**
     * Renews the session lock if this receiver is a session receiver.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver or if receiver is already disposed.
     * @throws ServiceBusException if the session lock cannot be renewed.
     */
    public Mono<OffsetDateTime> renewSessionLock() {
        return renewSessionLock(receiverOptions.getSessionId());
    }

    /**
     * Starts the auto lock renewal for the session this receiver works for.
     *
     * @param maxLockRenewalDuration Maximum duration to keep renewing the session lock.
     *
     * @return A lock renewal operation for the message.
     *
     * @throws NullPointerException if {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a non-session receiver or the receiver is disposed.
     * @throws ServiceBusException if the session lock renewal operation cannot be started.
     */
    public Mono<Void> renewSessionLock(Duration maxLockRenewalDuration) {
        return this.renewSessionLock(receiverOptions.getSessionId(), maxLockRenewalDuration);
    }

    /**
     * Sets the state of the session this receiver works for.
     *
     * @param sessionState State to set on the session.
     *
     * @return A Mono that completes when the session is set
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session state cannot be set.
     */
    public Mono<Void> setSessionState(byte[] sessionState) {
        return this.setSessionState(receiverOptions.getSessionId(), sessionState);
    }

    /**
     * Starts a new service side transaction. The {@link ServiceBusTransactionContext transaction context} should be
     * passed to all operations that needs to be in this transaction.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext}
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if a transaction cannot be created.
     */
    public Mono<ServiceBusTransactionContext> createTransaction() {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "createTransaction")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
            .flatMap(transactionSession -> transactionSession.createTransaction())
            .map(transaction -> new ServiceBusTransactionContext(transaction.getTransactionId()));
    }

    /**
     * Commits the transaction and all the operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext}
     *
     * @param transactionContext The transaction to be commit.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the transaction could not be committed.
     */
    public Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "commitTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return connectionProcessor
            .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
            .flatMap(transactionSession -> transactionSession.commitTransaction(new AmqpTransaction(
                transactionContext.getTransactionId())));
    }

    /**
     * Rollbacks the transaction given and all operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext}
     *
     * @param transactionContext The transaction to rollback.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the transaction could not be rolled back.
     */
    public Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "rollbackTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return connectionProcessor
            .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
            .flatMap(transactionSession -> transactionSession.rollbackTransaction(new AmqpTransaction(
                transactionContext.getTransactionId())));
    }

    /**
     * Disposes of the consumer by closing the underlying links to the service.
     */
    @Override
    public void close() {
        if (isDisposed.get()) {
            return;
        }

        try {
            completionLock.acquire();
        } catch (InterruptedException e) {
            logger.info("Unable to obtain completion lock.", e);
        }

        if (isDisposed.getAndSet(true)) {
            return;
        }

        // Blocking until the last message has been completed.
        logger.info("Removing receiver links.");
        final ServiceBusAsyncConsumer disposed = consumer.getAndSet(null);
        if (disposed != null) {
            disposed.close();
        }

        if (sessionManager != null) {
            sessionManager.close();
        }

        managementNodeLocks.close();
        renewalContainer.close();

        onClientClose.run();
    }

    /**
     * @return receiver options set by user;
     */
    ReceiverOptions getReceiverOptions() {
        return receiverOptions;
    }

    /**
     * Gets whether or not the management node contains the message lock token and it has not expired. Lock tokens are
     * held by the management node when they are received from the management node or management operations are
     * performed using that {@code lockToken}.
     *
     * @param lockToken Lock token to check for.
     *
     * @return {@code true} if the management node contains the lock token and false otherwise.
     */
    private boolean isManagementToken(String lockToken) {
        return managementNodeLocks.containsUnexpired(lockToken);
    }

    private Mono<Void> updateDisposition(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, dispositionStatus.getValue())));
        } else if (Objects.isNull(message)) {
            return monoError(logger, new NullPointerException("'message' cannot be null."));
        }

        final String lockToken = message.getLockToken();
        final String sessionId = message.getSessionId();

        if (receiverOptions.getReceiveMode() != ServiceBusReceiveMode.PEEK_LOCK) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (message.isSettled()) {
            return Mono.error(logger.logExceptionAsError(
                new IllegalArgumentException("The message has either been deleted or already settled.")));
        } else if (message.getLockToken() == null) {
            // message must be a peeked message (or somehow they created a message w/o a lock token)
            final String errorMessage = "This operation is not supported for peeked messages. "
                + "Only messages received using receiveMessages() in PEEK_LOCK mode can be settled.";
            return Mono.error(
                logger.logExceptionAsError(new UnsupportedOperationException(errorMessage))
            );
        }

        final String sessionIdToUse;
        if (sessionId == null && !CoreUtils.isNullOrEmpty(receiverOptions.getSessionId())) {
            sessionIdToUse = receiverOptions.getSessionId();
        } else {
            sessionIdToUse = sessionId;
        }

        logger.verbose("{}: Update started. Disposition: {}. Lock: {}. SessionId: {}.", entityPath, dispositionStatus,
            lockToken, sessionIdToUse);

        // This operation is not kicked off until it is subscribed to.
        final Mono<Void> performOnManagement = connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                deadLetterErrorDescription, propertiesToModify, sessionId, getLinkName(sessionId), transactionContext))
            .then(Mono.fromRunnable(() -> {
                logger.info("{}: Management node Update completed. Disposition: {}. Lock: {}.",
                    entityPath, dispositionStatus, lockToken);

                message.setIsSettled();
                managementNodeLocks.remove(lockToken);
                renewalContainer.remove(lockToken);
            }));

        Mono<Void> updateDispositionOperation;
        if (sessionManager != null) {
            updateDispositionOperation = sessionManager.updateDisposition(lockToken, sessionId, dispositionStatus,
                propertiesToModify, deadLetterReason, deadLetterErrorDescription, transactionContext)
                .flatMap(isSuccess -> {
                    if (isSuccess) {
                        message.setIsSettled();
                        renewalContainer.remove(lockToken);
                        return Mono.empty();
                    }

                    logger.info("Could not perform on session manger. Performing on management node.");
                    return performOnManagement;
                });
        } else {
            final ServiceBusAsyncConsumer existingConsumer = consumer.get();
            if (isManagementToken(lockToken) || existingConsumer == null) {
                updateDispositionOperation = performOnManagement;
            } else {
                updateDispositionOperation = existingConsumer.updateDisposition(lockToken, dispositionStatus,
                    deadLetterReason, deadLetterErrorDescription, propertiesToModify, transactionContext)
                    .then(Mono.fromRunnable(() -> {
                        logger.verbose("{}: Update completed. Disposition: {}. Lock: {}.",
                            entityPath, dispositionStatus, lockToken);

                        message.setIsSettled();
                        renewalContainer.remove(lockToken);
                    }));
            }
        }

        return updateDispositionOperation
            .onErrorMap(throwable -> {
                if (throwable instanceof ServiceBusException) {
                    return throwable;
                }

                switch (dispositionStatus) {
                    case COMPLETED:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.COMPLETE);
                    case ABANDONED:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.ABANDON);
                    default:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.UNKNOWN);
                }
            });
    }

    private ServiceBusAsyncConsumer getOrCreateConsumer() {
        final ServiceBusAsyncConsumer existing = consumer.get();
        if (existing != null) {
            return existing;
        }

        final String linkName = StringUtil.getRandomString(entityPath);
        logger.info("{}: Creating consumer for link '{}'", entityPath, linkName);

        // Use withRetry below to retry transient errors like connection lost.
        final Flux<ServiceBusReceiveLink> receiveLink = withRetry(connectionProcessor.flatMap(connection -> {
            if (receiverOptions.isSessionReceiver()) {
                return connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                    null, entityType, receiverOptions.getSessionId());
            } else {
                return connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                    null, entityType);
            }
        })
            .doOnNext(next -> {
                final String format = "Created consumer for Service Bus resource: [{}] mode: [{}]"
                    + " sessionEnabled? {} transferEntityPath: [{}], entityType: [{}]";
                logger.verbose(format, next.getEntityPath(), receiverOptions.getReceiveMode(),
                    CoreUtils.isNullOrEmpty(receiverOptions.getSessionId()), "N/A", entityType);
            }),
            connectionProcessor.getRetryOptions(), "Failed to create receive link " + linkName, true)
            .repeat();

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLink.subscribeWith(
            new ServiceBusReceiveLinkProcessor(receiverOptions.getPrefetchCount(), retryPolicy));

        final ServiceBusAsyncConsumer newConsumer = new ServiceBusAsyncConsumer(linkName, linkMessageProcessor,
            messageSerializer, receiverOptions);

        // There could have been multiple threads trying to create this async consumer when the result was null.
        // If another one had set the value while we were creating this resource, dispose of newConsumer.
        if (consumer.compareAndSet(null, newConsumer)) {
            return newConsumer;
        } else {
            newConsumer.close();
            return consumer.get();
        }
    }

    /**
     * If the receiver has not connected via {@link #receiveMessages()}, all its current operations have been performed
     * through the management node.
     *
     * @return The name of the receive link, or null of it has not connected via a receive link.
     */
    private String getLinkName(String sessionId) {
        if (sessionManager != null && !CoreUtils.isNullOrEmpty(sessionId)) {
            return sessionManager.getLinkName(sessionId);
        } else if (!CoreUtils.isNullOrEmpty(sessionId) && !receiverOptions.isSessionReceiver()) {
            return null;
        } else {
            final ServiceBusAsyncConsumer existing = consumer.get();
            return existing != null ? existing.getLinkName() : null;
        }
    }

    Mono<OffsetDateTime> renewSessionLock(String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewSessionLock")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot renew session lock on a non-session receiver."));
        }
        final String linkName = sessionManager != null
            ? sessionManager.getLinkName(sessionId)
            : null;

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(channel -> channel.renewSessionLock(sessionId, linkName))
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    Mono<Void> renewSessionLock(String sessionId, Duration maxLockRenewalDuration) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewSessionLock")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException(
                "Cannot renew session lock on a non-session receiver."));
        } else if (maxLockRenewalDuration == null) {
            return monoError(logger, new NullPointerException("'maxLockRenewalDuration' cannot be null."));
        } else if (maxLockRenewalDuration.isNegative()) {
            return monoError(logger, new IllegalArgumentException(
                "'maxLockRenewalDuration' cannot be negative."));
        } else if (Objects.isNull(sessionId)) {
            return monoError(logger, new NullPointerException("'sessionId' cannot be null."));
        } else if (sessionId.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'sessionId' cannot be empty."));
        }
        final LockRenewalOperation operation = new LockRenewalOperation(sessionId,
            maxLockRenewalDuration, true, this::renewSessionLock);

        renewalContainer.addOrUpdate(sessionId, OffsetDateTime.now().plus(maxLockRenewalDuration), operation);
        return operation.getCompletionOperation()
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    Mono<Void> setSessionState(String sessionId, byte[] sessionState) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "setSessionState")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot set session state on a non-session receiver."));
        }
        final String linkName = sessionManager != null
            ? sessionManager.getLinkName(sessionId)
            : null;

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(channel -> channel.setSessionState(sessionId, sessionState, linkName))
            .onErrorMap((err) -> mapError(err, ServiceBusErrorSource.RECEIVE));
    }

    Mono<byte[]> getSessionState(String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getSessionState")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot get session state on a non-session receiver."));
        }

        Mono<byte[]> result;

        if (sessionManager != null) {
            result = sessionManager.getSessionState(sessionId);
        } else {
            result = connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(channel -> channel.getSessionState(sessionId, getLinkName(sessionId)));
        }

        return result.onErrorMap((err) -> mapError(err, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Map the error to {@link ServiceBusException}
     */
    private Throwable mapError(Throwable throwable, ServiceBusErrorSource errorSource) {
        if (!(throwable instanceof ServiceBusException)) {
            return new ServiceBusException(throwable, errorSource);
        }
        return throwable;
    }

    boolean isConnectionClosed() {
        return this.connectionProcessor.isChannelClosed();
    }

    boolean isManagementNodeLocksClosed() {
        return this.managementNodeLocks.isClosed();
    }

    boolean isRenewalContainerClosed() {
        return this.renewalContainer.isClosed();
    }
}
