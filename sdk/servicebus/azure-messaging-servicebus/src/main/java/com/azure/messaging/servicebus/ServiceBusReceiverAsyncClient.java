// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
 * <p><strong>Create an instance of receiver using default credential</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiateWithDefaultCredential}
 *
 * <p><strong>Receive all messages from Service Bus resource</strong></p>
 * <p>This returns an infinite stream of messages from Service Bus. The stream ends when the subscription is disposed or
 * other terminal scenarios. See {@link #receive()} for more information.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#all}
 *
 * <p><strong>Receive a maximum number of messages or until max a Duration</strong></p>
 * <p>This receives at most 15 messages, or until a duration of 30 seconds elapses. Whichever occurs first.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#int-duration}
 *
 * <p><strong>Receive messages in {@link ReceiveMode#RECEIVE_AND_DELETE} mode from Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.receiveWithReceiveAndDeleteMode}
 *
 * <p><strong>Receive messages from a specific session</strong></p>
 * <p>To fetch messages from a specific session, set {@link ServiceBusSessionReceiverClientBuilder#sessionId(String)}.
 * </p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#sessionId}
 *
 * <p><strong>Process messages from multiple sessions</strong></p>
 * <p>To process messages from multiple sessions, set
 * {@link ServiceBusSessionReceiverClientBuilder#maxConcurrentSessions(int)}. This will process in parallel at most
 * {@code maxConcurrentSessions}. In addition, when all the messages in a session have been consumed, it will find the
 * next available session to process.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#multiplesessions}
 *
 * <p><strong>Process messages from the first available session</strong></p>
 * <p>To process messages from the first available session, switch to {@link ServiceBusSessionReceiverClientBuilder} and
 * build the receiver client. It will find the first available session to process messages from.</p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#singlesession}
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
public final class ServiceBusReceiverAsyncClient implements AutoCloseable {
    private static final DeadLetterOptions DEFAULT_DEAD_LETTER_OPTIONS = new DeadLetterOptions();
    private static final String TRANSACTION_LINK_NAME = "coordinator";

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final MessageLockContainer managementNodeLocks;
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final UnnamedSessionManager unnamedSessionManager;

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

        this.managementNodeLocks = new MessageLockContainer(cleanupInterval);
        this.unnamedSessionManager = null;
    }

    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        ReceiverOptions receiverOptions, ServiceBusConnectionProcessor connectionProcessor, Duration cleanupInterval,
        TracerProvider tracerProvider, MessageSerializer messageSerializer, Runnable onClientClose,
        UnnamedSessionManager unnamedSessionManager) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.unnamedSessionManager = Objects.requireNonNull(unnamedSessionManager, "'sessionManager' cannot be null.");

        this.managementNodeLocks = new MessageLockContainer(cleanupInterval);
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
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken) {
        return abandon(lockToken, receiverOptions.getSessionId());
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token. This will make the message available
     * again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to abandon. {@code null} if there is no session.
     *
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken, String sessionId) {
        return abandon(lockToken, null, sessionId);
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
        return abandon(lockToken, propertiesToModify, receiverOptions.getSessionId());
    }

    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token and updates the message's properties.
     * This will make the message available again for processing. Abandoning a message will increase the delivery count
     * on the message.
     * <p><strong>Complete a message with a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.abandonMessageWithTransaction}
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Properties to modify on the message.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {
        return abandon(lockToken, propertiesToModify, receiverOptions.getSessionId(), transactionContext);
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
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify, String sessionId) {
        return updateDisposition(lockToken, DispositionStatus.ABANDONED, null, null,
            propertiesToModify, sessionId, null);
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
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(lockToken, DispositionStatus.ABANDONED, null, null,
            propertiesToModify, sessionId, transactionContext);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     * mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> complete(MessageLockToken lockToken) {
        if (lockToken instanceof ServiceBusReceivedMessage) {
            return complete(lockToken, ((ServiceBusReceivedMessage) lockToken).getSessionId());
        } else {
            return updateDisposition(lockToken, DispositionStatus.COMPLETED, null, null,
                null, null, null);
        }
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     * <p><strong>Complete a message with a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.completeMessageWithTransaction}
     *
     * @param lockToken Lock token of the message.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> complete(MessageLockToken lockToken, ServiceBusTransactionContext transactionContext) {
        return complete(lockToken, receiverOptions.getSessionId(), transactionContext);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to complete. {@code null} if there is no session.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> complete(MessageLockToken lockToken, String sessionId) {
        return updateDisposition(lockToken, DispositionStatus.COMPLETED, null, null,
            null, sessionId, null);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to complete. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> complete(MessageLockToken lockToken, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(lockToken, DispositionStatus.COMPLETED, null, null,
            null, sessionId, transactionContext);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token. This will move message into the deferred
     * subqueue.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus defer operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken) {
        return defer(lockToken, receiverOptions.getSessionId());
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token. This will move message into the deferred
     * subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     *
     * @return A {@link Mono} that completes when the defer operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, String sessionId) {
        return defer(lockToken, null, sessionId);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     *
     * @return A {@link Mono} that completes when the defer operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {
        return defer(lockToken, propertiesToModify, receiverOptions.getSessionId());
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the defer operation finishes.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {
        return defer(lockToken, propertiesToModify, receiverOptions.getSessionId(), transactionContext);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     *
     * @return A {@link Mono} that completes when the Service Bus defer operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify, String sessionId) {
        return updateDisposition(lockToken, DispositionStatus.DEFERRED, null, null,
            propertiesToModify, sessionId, null);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} using its lock token with modified message property. This will
     * move message into the deferred subqueue.
     *
     * @param lockToken Lock token of the message.
     * @param propertiesToModify Message properties to modify.
     * @param sessionId Session id of the message to defer. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the Service Bus defer operation finishes.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(lockToken, DispositionStatus.DEFERRED, null, null,
            propertiesToModify, sessionId, transactionContext);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken) {
        return deadLetter(lockToken, receiverOptions.getSessionId());
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, String sessionId) {
        return deadLetter(lockToken, DEFAULT_DEAD_LETTER_OPTIONS, sessionId);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        return deadLetter(lockToken, DEFAULT_DEAD_LETTER_OPTIONS, sessionId, transactionContext);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken} or {@code deadLetterOptions} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions) {
        return deadLetter(lockToken, deadLetterOptions, receiverOptions.getSessionId());
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken}, {@code deadLetterOptions}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions,
        ServiceBusTransactionContext transactionContext) {
        return deadLetter(lockToken, deadLetterOptions, receiverOptions.getSessionId(), transactionContext);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions, String sessionId) {
        if (Objects.isNull(deadLetterOptions)) {
            return monoError(logger, new NullPointerException("'deadLetterOptions' cannot be null."));
        }

        return updateDisposition(lockToken, DispositionStatus.SUSPENDED, deadLetterOptions.getDeadLetterReason(),
            deadLetterOptions.getDeadLetterErrorDescription(), deadLetterOptions.getPropertiesToModify(), sessionId,
            null);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter subqueue with deadletter reason, error
     * description, and/or modified properties.
     *
     * @param lockToken Lock token of the message.
     * @param deadLetterOptions The options to specify when moving message to the deadletter sub-queue.
     * @param sessionId Session id of the message to deadletter. {@code null} if there is no session.
     * @param transactionContext in which this operation is taking part in. The transaction should be created first by
     * {@link ServiceBusReceiverAsyncClient#createTransaction()} or
     * {@link ServiceBusSenderAsyncClient#createTransaction()}.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions, String sessionId,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(logger, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(logger, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(lockToken, DispositionStatus.SUSPENDED, deadLetterOptions.getDeadLetterReason(),
            deadLetterOptions.getDeadLetterErrorDescription(), deadLetterOptions.getPropertiesToModify(), sessionId,
            transactionContext);
    }

    /**
     * Gets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The session state or an empty Mono if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public Mono<byte[]> getSessionState(String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getSessionState")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot get session state on a non-session receiver."));
        }

        if (unnamedSessionManager != null) {
            return unnamedSessionManager.getSessionState(sessionId);
        } else {
            return connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(channel -> channel.getSessionState(sessionId, getLinkName(sessionId)));
        }
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
        return peek(receiverOptions.getSessionId());
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
    public Mono<ServiceBusReceivedMessage> peek(String sessionId) {
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
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekAt(long sequenceNumber) {
        return peekAt(sequenceNumber, receiverOptions.getSessionId());
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
    public Mono<ServiceBusReceivedMessage> peekAt(long sequenceNumber, String sessionId) {
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
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {
        return peekBatch(maxMessages, receiverOptions.getSessionId());
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
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, String sessionId) {
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
                        ServiceBusReceivedMessage emptyMessage = new ServiceBusReceivedMessage(new byte[0]);
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
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekBatchAt(int maxMessages, long sequenceNumber) {
        return peekBatchAt(maxMessages, sequenceNumber, receiverOptions.getSessionId());
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
    public Flux<ServiceBusReceivedMessage> peekBatchAt(int maxMessages, long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatchAt")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peek(sequenceNumber, sessionId, getLinkName(sessionId), maxMessages));
    }

    /**
     * Receives an <b>infinite</b> stream of {@link ServiceBusReceivedMessage messages} from the Service Bus
     * entity. This Flux continuously receives messages from a Service Bus entity until either:
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
    public Flux<ServiceBusReceivedMessageContext> receive() {
        if (unnamedSessionManager != null) {
            return unnamedSessionManager.receive();
        } else {
            return getOrCreateConsumer().receive().map(ServiceBusReceivedMessageContext::new);
        }
    }

    /**
     * Receives a bounded stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity. This stream
     * receives either {@code maxNumberOfMessages} are received or the {@code maxWaitTime} has elapsed.
     *
     * @param maxNumberOfMessages Maximum number of messages to receive.
     * @param maxWaitTime Maximum time to wait.
     *
     * @return A bounded {@link Flux} of messages.
     * @throws NullPointerException if {@code maxWaitTime} is null.
     * @throws IllegalArgumentException if {@code maxNumberOfMessages} is less than 1. {@code maxWaitTime} is zero
     *     or a negative duration.
     */
    public Flux<ServiceBusReceivedMessageContext> receive(int maxNumberOfMessages, Duration maxWaitTime) {
        if (maxNumberOfMessages < 1) {
            return fluxError(logger, new IllegalArgumentException("'maxNumberOfMessages' cannot be less than 1."));
        } else if (maxWaitTime == null) {
            return fluxError(logger, new NullPointerException("'maxWaitTime' cannot be null."));
        } else if (maxWaitTime.isNegative() || maxWaitTime.isZero()) {
            return fluxError(logger, new NullPointerException("'maxWaitTime' cannot be negative or zero."));
        }

        return receive().take(maxNumberOfMessages).take(maxWaitTime);
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
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber, String sessionId) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.receiveDeferredMessages(receiverOptions.getReceiveMode(),
                sessionId, getLinkName(sessionId), Collections.singleton(sequenceNumber)).last())
            .map(receivedMessage -> {
                if (CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                    return receivedMessage;
                }
                if (receiverOptions.getReceiveMode() == ReceiveMode.PEEK_LOCK) {
                    receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
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
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers) {
        return receiveDeferredMessageBatch(sequenceNumbers, receiverOptions.getSessionId());
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
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers,
        String sessionId) {
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
                if (receiverOptions.getReceiveMode() == ReceiveMode.PEEK_LOCK) {
                    receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
                        receivedMessage.getLockedUntil()));
                }

                return receivedMessage;
            });
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
     * @throws IllegalStateException if the receiver is a session receiver.
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
        } else if (receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException(
                String.format("Cannot renew message lock [%s] for a session receiver.", lockToken.getLockToken())));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(lockToken.getLockToken(), getLinkName(null)))
            .map(instant -> {
                if (lockToken instanceof ServiceBusReceivedMessage) {
                    ((ServiceBusReceivedMessage) lockToken).setLockedUntil(instant);
                }

                return managementNodeLocks.addOrUpdate(lockToken.getLockToken(), instant);
            });
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public Mono<Instant> renewSessionLock(String sessionId) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewSessionLock")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot renew session lock on a non-session receiver."));
        }

        final String linkName = unnamedSessionManager != null
            ? unnamedSessionManager.getLinkName(sessionId)
            : null;

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(channel -> channel.renewSessionLock(sessionId, linkName));
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     * @param sessionState State to set on the session.
     *
     * @return A Mono that completes when the session is set
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    public Mono<Void> setSessionState(String sessionId, byte[] sessionState) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "setSessionState")));
        } else if (!receiverOptions.isSessionReceiver()) {
            return monoError(logger, new IllegalStateException("Cannot set session state on a non-session receiver."));
        }

        final String linkName = unnamedSessionManager != null
            ? unnamedSessionManager.getLinkName(sessionId)
            : null;

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(channel -> channel.setSessionState(sessionId, sessionState, linkName));

    }

    /**
     * Starts a new service side transaction. The {@link ServiceBusTransactionContext} should be passed to all
     * operations that needs to be in this transaction.
     *
     * <p><strong>Create a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.createTransaction}
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
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
     * Commits the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     * <p><strong>Commit a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.commitTransaction}
     *
     * @param transactionContext to be committed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
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
     * Rollbacks the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     * <p><strong>Rollback a transaction</strong></p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusasyncreceiverclient.rollbackTransaction}
     *
     * @param transactionContext to be rollbacked.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
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
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        logger.info("Removing receiver links.");
        final ServiceBusAsyncConsumer disposed = consumer.getAndSet(null);
        if (disposed != null) {
            disposed.close();
        }

        if (unnamedSessionManager != null) {
            unnamedSessionManager.close();
        }

        onClientClose.run();
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
        return managementNodeLocks.contains(lockToken);
    }

    private Mono<Void> updateDisposition(MessageLockToken message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        String sessionId, ServiceBusTransactionContext transactionContext) {

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

        if (receiverOptions.getReceiveMode() != ReceiveMode.PEEK_LOCK) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(logger, new NullPointerException("'receivedMessage.lockToken' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'message.lockToken' cannot be empty."));
        }

        final String lockToken = message.getLockToken();
        final String sessionIdToUse;
        if (message instanceof ServiceBusReceivedMessage) {
            sessionIdToUse = ((ServiceBusReceivedMessage) message).getSessionId();
            if (!CoreUtils.isNullOrEmpty(sessionIdToUse) && !CoreUtils.isNullOrEmpty(sessionId)
                && !sessionIdToUse.equals(sessionId)) {
                logger.warning("Given sessionId '{}' does not match message's sessionId '{}'",
                    sessionId, sessionIdToUse);
            }
        } else if (sessionId == null && !CoreUtils.isNullOrEmpty(receiverOptions.getSessionId())) {
            sessionIdToUse = receiverOptions.getSessionId();
        } else {
            sessionIdToUse = sessionId;
        }

        logger.info("{}: Update started. Disposition: {}. Lock: {}. SessionId {}.", entityPath, dispositionStatus,
            lockToken, sessionIdToUse);

        final Mono<Void> performOnManagement = connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                deadLetterErrorDescription, propertiesToModify, sessionId, getLinkName(sessionId), transactionContext))
            .then(Mono.fromRunnable(() -> {
                logger.info("{}: Management node Update completed. Disposition: {}. Lock: {}.",
                    entityPath, dispositionStatus, lockToken);

                managementNodeLocks.remove(lockToken);
            }));

        if (unnamedSessionManager != null) {
            return unnamedSessionManager.updateDisposition(message, sessionId, dispositionStatus, propertiesToModify,
                deadLetterReason, deadLetterErrorDescription, transactionContext)
                .flatMap(isSuccess -> {
                    if (isSuccess) {
                        return Mono.empty();
                    }

                    logger.info("Could not perform on session manger. Performing on management node.");
                    return performOnManagement;
                });
        }

        final ServiceBusAsyncConsumer existingConsumer = consumer.get();
        if (isManagementToken(lockToken) || existingConsumer == null) {
            return performOnManagement;
        } else {
            return existingConsumer.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                deadLetterErrorDescription, propertiesToModify, transactionContext)
                .then(Mono.fromRunnable(() -> logger.info("{}: Update completed. Disposition: {}. Lock: {}.",
                    entityPath, dispositionStatus, lockToken)));
        }
    }

    private ServiceBusAsyncConsumer getOrCreateConsumer() {
        final ServiceBusAsyncConsumer existing = consumer.get();
        if (existing != null) {
            return existing;
        }

        final String linkName = StringUtil.getRandomString(entityPath);
        logger.info("{}: Creating consumer for link '{}'", entityPath, linkName);

        final Flux<ServiceBusReceiveLink> receiveLink = connectionProcessor.flatMap(connection -> {
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
            })
            .repeat();

        final LinkErrorContext context = new LinkErrorContext(fullyQualifiedNamespace, entityPath, linkName,
            null);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLink.subscribeWith(
            new ServiceBusReceiveLinkProcessor(receiverOptions.getPrefetchCount(), retryPolicy, connectionProcessor,
                context));
        final ServiceBusAsyncConsumer newConsumer = new ServiceBusAsyncConsumer(linkName, linkMessageProcessor,
            messageSerializer, false, receiverOptions.autoLockRenewalEnabled(),
            receiverOptions.getMaxAutoLockRenewalDuration(), connectionProcessor.getRetryOptions(),
            (token, associatedLinkName) -> renewMessageLock(token, associatedLinkName));

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
     * @return receiver options set by user;
     */
    ReceiverOptions getReceiverOptions() {
        return receiverOptions;
    }

    /**
     * Renews the message lock, and updates its value in the container.
     */
    private Mono<Instant> renewMessageLock(MessageLockToken lockToken, String linkName) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(lockToken.getLockToken(), linkName))
            .map(instant -> {
                if (lockToken instanceof ServiceBusReceivedMessage) {
                    ((ServiceBusReceivedMessage) lockToken).setLockedUntil(instant);
                }
                return instant;
            });
    }

    /**
     * If the receiver has not connected via {@link #receive()}, all its current operations have been performed through
     * the management node.
     *
     * @return The name of the receive link, or null of it has not connected via a receive link.
     */
    private String getLinkName(String sessionId) {
        if (unnamedSessionManager != null && !CoreUtils.isNullOrEmpty(sessionId)) {
            return unnamedSessionManager.getLinkName(sessionId);
        } else if (!CoreUtils.isNullOrEmpty(sessionId) && !receiverOptions.isSessionReceiver()) {
            return null;
        } else {
            final ServiceBusAsyncConsumer existing = consumer.get();
            return existing != null ? existing.getLinkName() : null;
        }
    }
}
