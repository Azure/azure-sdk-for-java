// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.administration.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token. This will make the message available
     * again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Lock token of the message.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void abandon(String lockToken) {
        asyncClient.abandon(lockToken).block(operationTimeout);
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void abandon(String lockToken, Map<String, Object> propertiesToModify) {
        asyncClient.abandon(lockToken, propertiesToModify).block(operationTimeout);
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void abandon(String lockToken, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        asyncClient.abandon(lockToken, propertiesToModify, transactionContext).block(operationTimeout);
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     * @param sessionId Session id of the message to abandon. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void abandon(String lockToken, Map<String, Object> propertiesToModify, String sessionId) {
        asyncClient.abandon(lockToken, propertiesToModify, sessionId).block(operationTimeout);
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     * @param sessionId Session id of the message to abandon. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void abandon(String lockToken, Map<String, Object> propertiesToModify, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        asyncClient.abandon(lockToken, propertiesToModify, sessionId, transactionContext).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void complete(String lockToken) {
        asyncClient.complete(lockToken).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void complete(String lockToken, ServiceBusTransactionContext transactionContext) {
        asyncClient.complete(lockToken, transactionContext).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to complete. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void complete(String lockToken, String sessionId) {
        asyncClient.complete(lockToken, sessionId).block(operationTimeout);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to complete. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void complete(String lockToken, String sessionId, ServiceBusTransactionContext
        transactionContext) {
        asyncClient.complete(lockToken, sessionId, transactionContext).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token. This will move message into the deferred
     * subqueue.
     *
     * @param lockToken Lock token of the message.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken) {
        asyncClient.defer(lockToken).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token. This will move message into the deferred
     * subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken, String sessionId) {
        asyncClient.defer(lockToken, sessionId).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken, Map<String, Object> propertiesToModify) {
        asyncClient.defer(lockToken, propertiesToModify).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken, Map<String, Object> propertiesToModify, ServiceBusTransactionContext
        transactionContext) {
        asyncClient.defer(lockToken, propertiesToModify, transactionContext).block(operationTimeout);

    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken, Map<String, Object> propertiesToModify, String sessionId) {
        asyncClient.defer(lockToken, propertiesToModify, sessionId).block(operationTimeout);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public void defer(String lockToken, Map<String, Object> propertiesToModify, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        asyncClient.defer(lockToken, propertiesToModify, sessionId, transactionContext).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public void deadLetter(String lockToken) {
        asyncClient.deadLetter(lockToken).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public void deadLetter(String lockToken, String sessionId) {
        asyncClient.deadLetter(lockToken, sessionId).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void deadLetter(String lockToken, DeadLetterOptions deadLetterOptions) {
        asyncClient.deadLetter(lockToken, deadLetterOptions).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void deadLetter(String lockToken, DeadLetterOptions deadLetterOptions,
        ServiceBusTransactionContext transactionContext) {
        asyncClient.deadLetter(lockToken, deadLetterOptions, transactionContext).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     *
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void deadLetter(String lockToken, DeadLetterOptions deadLetterOptions, String sessionId) {
        asyncClient.deadLetter(lockToken, deadLetterOptions, sessionId).block(operationTimeout);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverClient#createTransaction()} or {@link ServiceBusSenderClient#createTransaction()}.
     *
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     */
    public void deadLetter(String lockToken, DeadLetterOptions deadLetterOptions, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        asyncClient.deadLetter(lockToken, deadLetterOptions, sessionId, transactionContext).block(operationTimeout);
    }

    /**
     * Gets and starts the auto lock renewal for a message with the given lock.
     *
     * @param lockToken Lock token of the message.
     * @param maxLockRenewalDuration Maximum duration to keep renewing the lock token.
     * @return A lock renewal operation for the message.
     * @throws NullPointerException if {@code lockToken} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalArgumentException if {@code lockToken} is an empty string.
     * @throws IllegalStateException if the receiver is a session receiver or the receiver is disposed.
     */
    public LockRenewalOperation getAutoRenewMessageLock(String lockToken, Duration maxLockRenewalDuration) {
        return asyncClient.getAutoRenewMessageLock(lockToken, maxLockRenewalDuration);
    }

    /**
     * Gets and starts the auto lock renewal for a session with the given lock.
     *
     * @param sessionId Id for the session to renew.
     * @param maxLockRenewalDuration Maximum duration to keep renewing the lock token.
     * @return A lock renewal operation for the message.
     * @throws NullPointerException if {@code sessionId} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalArgumentException if {@code lockToken} is an empty string.
     * @throws IllegalStateException if the receiver is a non-session receiver or the receiver is disposed.
     */
    public LockRenewalOperation getAutoRenewSessionLock(String sessionId, Duration maxLockRenewalDuration) {
        return asyncClient.getAutoRenewSessionLock(sessionId, maxLockRenewalDuration);
    }

    /**
     * Gets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The session state or null if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public byte[] getSessionState(String sessionId) {
        return asyncClient.getSessionState(sessionId).block(operationTimeout);
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public ServiceBusReceivedMessage peekMessage() {
        return asyncClient.peekMessage().block(operationTimeout);
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public ServiceBusReceivedMessage peekMessage(String sessionId) {
        return asyncClient.peekMessage(sessionId).block(operationTimeout);
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
    public ServiceBusReceivedMessage peekMessageAt(long sequenceNumber) {
        return asyncClient.peekMessageAt(sequenceNumber).block(operationTimeout);
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public ServiceBusReceivedMessage peekMessageAt(long sequenceNumber, String sessionId) {
        return asyncClient.peekMessageAt(sequenceNumber, sessionId).block(operationTimeout);
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} that are peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessages(maxMessages)
            .timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} that are peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, String sessionId) {
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
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessagesAt(int maxMessages, long sequenceNumber) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessagesAt(maxMessages, sequenceNumber)
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
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public IterableStream<ServiceBusReceivedMessage> peekMessagesAt(int maxMessages, long sequenceNumber,
                                                                    String sessionId) {
        if (maxMessages <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessagesAt(maxMessages, sequenceNumber,
            sessionId).timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     * The receive operation will wait for a default 1 minute for receiving a message before it times out. You can it
     * override by using {@link #receiveMessages(int, Duration)}.
     *
     * @param maxMessages The maximum number of messages to receive.
     * @return An {@link IterableStream} of at most {@code maxMessages} messages from the Service Bus entity.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is zero or a negative value.
     */
    public IterableStream<ServiceBusReceivedMessageContext> receiveMessages(int maxMessages) {
        return receiveMessages(maxMessages, operationTimeout);
    }

    /**
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity. The
     * default receive mode is {@link ReceiveMode#PEEK_LOCK } unless it is changed during creation of
     * {@link ServiceBusReceiverClient} using {@link ServiceBusReceiverClientBuilder#receiveMode(ReceiveMode)}.
     *
     * @param maxMessages The maximum number of messages to receive.
     * @param maxWaitTime The time the client waits for receiving a message before it times out.
     * @return An {@link IterableStream} of at most {@code maxMessages} messages from the Service Bus entity.
     *
     * @throws IllegalArgumentException if {@code maxMessages} or {@code maxWaitTime} is zero or a negative value.
     */
    public IterableStream<ServiceBusReceivedMessageContext> receiveMessages(int maxMessages,
        Duration maxWaitTime) {
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

        final Flux<ServiceBusReceivedMessageContext> messages = Flux.create(emitter -> queueWork(maxMessages,
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
     */
    public ServiceBusReceivedMessage receiveDeferredMessage(long sequenceNumber) {
        return asyncClient.receiveDeferredMessage(sequenceNumber).block(operationTimeout);
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
     */
    public ServiceBusReceivedMessage receiveDeferredMessage(long sequenceNumber, String sessionId) {
        return asyncClient.receiveDeferredMessage(sequenceNumber, sessionId).block(operationTimeout);
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     *
     * @return An {@link IterableStream} of deferred {@link ServiceBusReceivedMessage messages}.
     */
    public IterableStream<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers) {
        final Flux<ServiceBusReceivedMessage> messages = asyncClient.receiveDeferredMessages(sequenceNumbers)
            .timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     * @param sessionId Session id of the deferred messages. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of deferred {@link ServiceBusReceivedMessage messages}.
     */
    public IterableStream<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers,
        String sessionId) {
        final Flux<ServiceBusReceivedMessage> messages = asyncClient.receiveDeferredMessages(sequenceNumbers,
            sessionId).timeout(operationTimeout);

        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(messages);
    }

    /**
     * Renews the lock on the specified message. The lock will be renewed based on the setting specified on the entity.
     * When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is locked on the server for this
     * receiver instance for a duration as specified during the Queue creation (LockDuration). If processing of the
     * message requires longer than this duration, the lock needs to be renewed. For each renewal, the lock is reset to
     * the entity's LockDuration value.
     *
     * @param lockToken Lock token of the message to renew.
     *
     * @return The new expiration time for the message.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@code lockToken} is an empty value.
     * @throws IllegalStateException if the receiver is a session receiver.
     */
    public Instant renewMessageLock(String lockToken) {
        return asyncClient.renewMessageLock(lockToken).block(operationTimeout);
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public Instant renewSessionLock(String sessionId) {
        return asyncClient.renewSessionLock(sessionId).block(operationTimeout);
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     * @param sessionState State to set on the session.
     *
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public void setSessionState(String sessionId, byte[] sessionState) {
        asyncClient.setSessionState(sessionId, sessionState).block(operationTimeout);
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along with
     * {@link ServiceBusReceivedMessage} or {@code lockToken} to all operations that needs to be in
     * this transaction.
     *
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     *
     * @return a new {@link ServiceBusTransactionContext}.
     */
    public ServiceBusTransactionContext createTransaction() {
        return asyncClient.createTransaction().block(operationTimeout);
    }

    /**
     * Commits the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext to be committed.
     *
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     */
    public void commitTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.commitTransaction(transactionContext).block(operationTimeout);
    }

    /**
     * Rollbacks the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext to be rollbacked.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     */
    public void rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.rollbackTransaction(transactionContext).block(operationTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        asyncClient.close();

        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.getAndSet(null);
        if (messageSubscriber != null && !messageSubscriber.isDisposed()) {
            messageSubscriber.dispose();
        }
    }

    /**
     * Given an {@code emitter}, creates a {@link SynchronousMessageSubscriber} to receive messages from Service Bus
     * entity.
     */
    private void queueWork(int maximumMessageCount, Duration maxWaitTime,
        FluxSink<ServiceBusReceivedMessageContext> emitter) {
        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maxWaitTime, emitter);

        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.get();
        if (messageSubscriber == null) {
            long prefetch = asyncClient.getReceiverOptions().getPrefetchCount();
            SynchronousMessageSubscriber newSubscriber = new SynchronousMessageSubscriber(prefetch, work);

            if (!synchronousMessageSubscriber.compareAndSet(null, newSubscriber)) {
                newSubscriber.dispose();
                SynchronousMessageSubscriber existing = synchronousMessageSubscriber.get();
                existing.queueWork(work);
            } else {
                asyncClient.receiveMessages().subscribeWith(newSubscriber);
            }
        } else {
            messageSubscriber.queueWork(work);
        }
        logger.verbose("[{}] Receive request queued up.", work.getId());
    }
}
