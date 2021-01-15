// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A <b>synchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue or
 * topic on Azure Service Bus.
 *
 * <p><strong>Create an instance of receiver</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation}
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverAsyncClient To communicate with a Service Bus resource using an asynchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusReceiverClient implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClient.class);
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final ServiceBusReceiverAsyncClient asyncClient;
    private final Duration operationTimeout;

    /* To hold each receive work item to be processed.*/
    private final AtomicReference<SynchronousMessageSubscriber> synchronousMessageSubscriber = new AtomicReference<>();

    /**
     * Creates a synchronous receiver given its asynchronous counterpart.
     *
     * @param asyncClient Asynchronous receiver.
     */
    ServiceBusReceiverClient(ServiceBusReceiverAsyncClient asyncClient, Duration operationTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
    }

    /**
     * Gets the fully qualified Service Bus namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Service Bus resource this client interacts with.
     *
     * @return The Service Bus resource this client interacts with.
     */
    public String getEntityPath() {
        return asyncClient.getEntityPath();
    }

    /**
     * Abandons a {@link ServiceBusReceivedMessage message}. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be abandoned.
     */
    public void abandon(ServiceBusReceivedMessage message) {
        asyncClient.abandon(message).block(operationTimeout);
    }

    /**
     * Abandons a {@link ServiceBusReceivedMessage message} and updates the message's properties. This will make the
     * message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options The options to set while abandoning the message.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws IllegalArgumentException if the message is already settled.
     * @throws ServiceBusException if the message could not be abandoned.
     */
    public void abandon(ServiceBusReceivedMessage message, AbandonOptions options) {
        asyncClient.abandon(message, options).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message}. This will delete the message from the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws IllegalArgumentException if the message is already settled.
     * @throws ServiceBusException if the message could not be completed.
     */
    public void complete(ServiceBusReceivedMessage message) {
        asyncClient.complete(message).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message}. This will delete the message from the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to complete the message.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws IllegalArgumentException if the message is already settled.
     * @throws ServiceBusException if the message could not be completed.
     */
    public void complete(ServiceBusReceivedMessage message, CompleteOptions options) {
        asyncClient.complete(message, options).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message}. This will move message into the deferred subqueue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     * @throws IllegalArgumentException if the message is already settled.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(ServiceBusReceivedMessage message) {
        asyncClient.defer(message).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This
     * will move message into the deferred sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to defer the message.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(ServiceBusReceivedMessage message, DeferOptions options) {
        asyncClient.defer(message, options).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be dead-lettered.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public void deadLetter(ServiceBusReceivedMessage message) {
        asyncClient.deadLetter(message).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue with dead-letter reason, error
     * description, and/or modified properties.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to dead-letter the message.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if the receiver is already disposed of.
     * @throws ServiceBusException if the message could not be dead-lettered.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public void deadLetter(ServiceBusReceivedMessage message, DeadLetterOptions options) {
        asyncClient.deadLetter(message, options).block(operationTimeout);
    }

    /**
     * Gets the state of the session if this receiver is a session receiver.
     *
     * @return The session state or null if there is no state set for the session.
     *
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session state could not be acquired.
     */
    public byte[] getSessionState() {
        return this.getSessionState(asyncClient.getReceiverOptions().getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peekMessage()} fetches the first active message for this receiver. Each subsequent call fetches the
     * subsequent message in the entity.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     *
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public ServiceBusReceivedMessage peekMessage() {
        return this.peekMessage(asyncClient.getReceiverOptions().getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peekMessage()} fetches the first active message for this receiver. Each subsequent call fetches the
     * subsequent message in the entity.
     *
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if receiver is already disposed or the receiver is a non-session receiver.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    ServiceBusReceivedMessage peekMessage(String sessionId) {
        return asyncClient.peekMessage(sessionId).block(operationTimeout);
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     *
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public ServiceBusReceivedMessage peekMessage(long sequenceNumber) {
        return this.peekMessage(sequenceNumber, asyncClient.getReceiverOptions().getSessionId());
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
    ServiceBusReceivedMessage peekMessage(long sequenceNumber, String sessionId) {
        return asyncClient.peekMessage(sequenceNumber, sessionId).block(operationTimeout);
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The maximum number of messages to peek.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} that are peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages) {
        return this.peekMessages(maxMessages, asyncClient.getReceiverOptions().getSessionId());
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
    IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, String sessionId) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessages(maxMessages, sessionId)
            .timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber) {
        return this.peekMessages(maxMessages, sequenceNumber, asyncClient.getReceiverOptions().getSessionId());
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
    IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber, String sessionId) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessages(maxMessages, sequenceNumber,
            sessionId).timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity. The
     * receive operation will wait for a default 1 minute for receiving a message before it times out. You can
     * override it by using {@link #receiveMessages(int, Duration)}.
     *
     * @param maxMessages The maximum number of messages to receive.
     *
     * @return An {@link IterableStream} of at most {@code maxMessages} messages from the Service Bus entity.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is zero or a negative value.
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while receiving messages.
     */
    public IterableStream<ServiceBusReceivedMessage> receiveMessages(int maxMessages) {
        return receiveMessages(maxMessages, operationTimeout);
    }

    /**
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity. The
     * default receive mode is {@link ServiceBusReceiveMode#PEEK_LOCK } unless it is changed during creation of {@link
     * ServiceBusReceiverClient} using {@link ServiceBusReceiverClientBuilder#receiveMode(ServiceBusReceiveMode)}.
     *
     * @param maxMessages The maximum number of messages to receive.
     * @param maxWaitTime The time the client waits for receiving a message before it times out.
     *
     * @return An {@link IterableStream} of at most {@code maxMessages} messages from the Service Bus entity.
     *
     * @throws IllegalArgumentException if {@code maxMessages} or {@code maxWaitTime} is zero or a negative value.
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if an error occurs while receiving messages.
     */
    public IterableStream<ServiceBusReceivedMessage> receiveMessages(int maxMessages, Duration maxWaitTime) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        } else if (Objects.isNull(maxWaitTime)) {
            throw logger.logExceptionAsError(
                new NullPointerException("'maxWaitTime' cannot be null."));
        } else if (maxWaitTime.isNegative() || maxWaitTime.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maxWaitTime' cannot be zero or less. maxWaitTime: " + maxWaitTime));
        }

        final Flux<ServiceBusReceivedMessage> messages = Flux.create(emitter -> queueWork(maxMessages,
            maxWaitTime, emitter));

        return new IterableStream<>(messages);
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
    public ServiceBusReceivedMessage receiveDeferredMessage(long sequenceNumber) {
        return this.receiveDeferredMessage(sequenceNumber, asyncClient.getReceiverOptions().getSessionId());
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     * @param sessionId Session id of the deferred message.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     * @throws IllegalStateException if receiver is already disposed.
     */
    ServiceBusReceivedMessage receiveDeferredMessage(long sequenceNumber, String sessionId) {
        return asyncClient.receiveDeferredMessage(sequenceNumber, sessionId).block(operationTimeout);
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     *
     * @return An {@link IterableStream} of deferred {@link ServiceBusReceivedMessage messages}.
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred messages cannot be received.
     */
    public IterableStream<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers) {
        return this.receiveDeferredMessageBatch(sequenceNumbers, asyncClient.getReceiverOptions().getSessionId());
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
    IterableStream<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers,
        String sessionId) {
        final Flux<ServiceBusReceivedMessage> messages = asyncClient.receiveDeferredMessages(sequenceNumbers,
            sessionId).timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Renews the lock on the specified message. The lock will be renewed based on the setting specified on the entity.
     * When a message is received in {@link ServiceBusReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the Queue creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal,
     * the lock is reset to the entity's LockDuration value.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform lock renewal.
     *
     * @return The new expiration time for the message.
     *
     * @throws NullPointerException if {@code message} or {@code message.getLockToken()} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *      {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from peekMessage.
     * @throws IllegalStateException if the receiver is a session receiver or receiver is already disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     */
    public OffsetDateTime renewMessageLock(ServiceBusReceivedMessage message) {
        return asyncClient.renewMessageLock(message).block(operationTimeout);
    }

    /**
     * Starts the auto lock renewal for a message with the given lock.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform auto-lock renewal.
     * @param maxLockRenewalDuration Maximum duration to keep renewing the lock token.
     * @param onError A function to call when an error occurs during lock renewal.
     *
     * @throws NullPointerException if {@code message} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a session receiver or the receiver is disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     * @throws ServiceBusException If the message cannot be renewed.
     */
    public void renewMessageLock(ServiceBusReceivedMessage message, Duration maxLockRenewalDuration,
        Consumer<Throwable> onError) {
        final String lockToken = message != null ? message.getLockToken() : "null";
        final Consumer<Throwable> throwableConsumer = onError != null
            ? onError
            : error -> logger.warning("Exception occurred while renewing lock token '{}'.", lockToken, error);

        asyncClient.renewMessageLock(message, maxLockRenewalDuration).subscribe(
            v -> logger.verbose("Completed renewing lock token: '{}'", lockToken),
            throwableConsumer,
            () -> logger.verbose("Auto message lock renewal operation completed: {}", lockToken));
    }

    /**
     * Sets the state of the session if this receiver is a session receiver.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session lock cannot be renewed.
     */
    public OffsetDateTime renewSessionLock() {
        return this.renewSessionLock(asyncClient.getReceiverOptions().getSessionId());
    }

    /**
     * Starts the auto lock renewal for the session that this receiver works for.
     *
     * @param maxLockRenewalDuration Maximum duration to keep renewing the session.
     * @param onError A function to call when an error occurs during lock renewal.
     *
     * @throws NullPointerException if {@code sessionId} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalArgumentException if {@code sessionId} is an empty string.
     * @throws IllegalStateException if the receiver is a non-session receiver or the receiver is disposed.
     */
    public void renewSessionLock(Duration maxLockRenewalDuration, Consumer<Throwable> onError) {
        this.renewSessionLock(asyncClient.getReceiverOptions().getSessionId(), maxLockRenewalDuration, onError);
    }

    /**
     * Sets the state of the session if this receiver is a session receiver.
     *
     * @param sessionState State to set on the session.
     *
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session state cannot be set.
     */
    public void setSessionState(byte[] sessionState) {
        this.setSessionState(asyncClient.getReceiverOptions().getSessionId(), sessionState);
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along to all
     * operations that need to be in this transaction.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext}
     *
     * @return A new {@link ServiceBusTransactionContext}.
     *
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws ServiceBusException if a transaction cannot be created.
     */
    public ServiceBusTransactionContext createTransaction() {
        return asyncClient.createTransaction().block(operationTimeout);
    }

    /**
     * Commits the transaction and all the operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext}
     *
     * @param transactionContext The transaction to be commit.
     *
     * @throws IllegalStateException if the receiver is already disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be committed.
     */
    public void commitTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.commitTransaction(transactionContext).block(operationTimeout);
    }

    /**
     * Rollbacks the transaction given and all operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext}
     *
     * @param transactionContext The transaction to be rollback.
     *
     * @throws IllegalStateException if the receiver is alread disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be rolled back.
     */
    public void rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.rollbackTransaction(transactionContext).block(operationTimeout);
    }

    /**
     * Disposes of the consumer by closing the underlying links to the service.
     */
    @Override
    public void close() {
        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.getAndSet(null);
        if (messageSubscriber != null && !messageSubscriber.isDisposed()) {
            messageSubscriber.dispose();
        }

        asyncClient.close();
    }

    /**
     * Given an {@code emitter}, creates a {@link SynchronousMessageSubscriber} to receive messages from Service Bus
     * entity.
     */
    private void queueWork(int maximumMessageCount, Duration maxWaitTime,
                           FluxSink<ServiceBusReceivedMessage> emitter) {
        final long id = idGenerator.getAndIncrement();
        final int prefetch = asyncClient.getReceiverOptions().getPrefetchCount();
        final int toRequest = prefetch != 0 ? Math.min(maximumMessageCount, prefetch) : maximumMessageCount;
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id,
            toRequest,
            maxWaitTime, emitter);
        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.get();
        if (messageSubscriber == null) {
            SynchronousMessageSubscriber newSubscriber = new SynchronousMessageSubscriber(toRequest, work);
            if (!synchronousMessageSubscriber.compareAndSet(null, newSubscriber)) {
                newSubscriber.dispose();
                SynchronousMessageSubscriber existing = synchronousMessageSubscriber.get();
                existing.queueWork(work);
            } else {
                asyncClient.receiveMessagesNoBackPressure().subscribeWith(newSubscriber);
            }
        } else {
            messageSubscriber.queueWork(work);
        }
        logger.verbose("[{}] Receive request queued up.", work.getId());
    }

    OffsetDateTime renewSessionLock(String sessionId) {
        return asyncClient.renewSessionLock(sessionId).block(operationTimeout);
    }

    void renewSessionLock(String sessionId, Duration maxLockRenewalDuration, Consumer<Throwable> onError) {
        final Consumer<Throwable> throwableConsumer = onError != null
            ? onError
            : error -> logger.warning("Exception occurred while renewing session: '{}'.", sessionId, error);

        asyncClient.renewSessionLock(maxLockRenewalDuration).subscribe(
            v -> logger.verbose("Completed renewing session: '{}'", sessionId),
            throwableConsumer,
            () -> logger.verbose("Auto session lock renewal operation completed: {}", sessionId));
    }

    void setSessionState(String sessionId, byte[] sessionState) {
        asyncClient.setSessionState(sessionId, sessionState).block(operationTimeout);
    }

    byte[] getSessionState(String sessionId) {
        return asyncClient.getSessionState(sessionId).block(operationTimeout);
    }
}
