// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.WORK_ID_KEY;

/**
 * A <b>synchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue or
 * topic on Azure Service Bus.
 *
 * <p><strong>Create an instance of receiver</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.instantiation -->
 * <pre>
 * &#47;&#47; The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
 * &#47;&#47; The connectionString&#47;queueName must be set by the application. The 'connectionString' format is shown below.
 * &#47;&#47; &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;
 * ServiceBusReceiverClient receiver = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .receiver&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Use the receiver and finally close it.
 * receiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.instantiation -->
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverAsyncClient To communicate with a Service Bus resource using an asynchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusReceiverClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverClient.class);
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final ServiceBusReceiverAsyncClient asyncClient;
    private final Duration operationTimeout;
    private final boolean isPrefetchDisabled;

    /* To hold each receive work item to be processed.*/
    private final AtomicReference<SynchronousMessageSubscriber> synchronousMessageSubscriber = new AtomicReference<>();
    /* To ensure synchronousMessageSubscriber is subscribed only once. */
    private final AtomicBoolean syncSubscribed = new AtomicBoolean(false);

    private final ServiceBusTracer tracer;
    /**
     * Creates a synchronous receiver given its asynchronous counterpart.
     *
     * @param asyncClient Asynchronous receiver.
     * @param isPrefetchDisabled Indicates if the prefetch is disabled.
     * @param operationTimeout Timeout to wait for operation to complete.
     */
    ServiceBusReceiverClient(ServiceBusReceiverAsyncClient asyncClient,
                             boolean isPrefetchDisabled,
                             Duration operationTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
        this.isPrefetchDisabled = isPrefetchDisabled;
        this.tracer = asyncClient.getInstrumentation().getTracer();
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
     * Gets the SessionId of the session if this receiver is a session receiver.
     *
     * @return The SessionId or null if this is not a session receiver.
     */
    public String getSessionId() {
        return asyncClient.getSessionId();
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusReceiverClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusReceiverClient}.
     */
    public String getIdentifier() {
        return asyncClient.getIdentifier();
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
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
     *
     * @throws IllegalStateException if receiver is already disposed or the receiver is a non-session receiver.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
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
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
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
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, String sessionId) {
        if (maxMessages <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessages(maxMessages, sessionId)
            .timeout(operationTimeout);

        final Flux<ServiceBusReceivedMessage> tracedMessages = tracer.traceSyncReceive("ServiceBus.peekMessages", messages);
        // Subscribe to message flux so we can kick off this operation, but not to tracing - caller subscriber will take care of it.
        messages.subscribe();

        return new IterableStream<>(tracedMessages);
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
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    IterableStream<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber, String sessionId) {
        if (maxMessages <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        }

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.peekMessages(maxMessages, sequenceNumber,
            sessionId).timeout(operationTimeout);

        final Flux<ServiceBusReceivedMessage> tracedMessages = tracer.traceSyncReceive("ServiceBus.peekMessages", messages);
        // Subscribe to message flux so we can kick off this operation, but not to tracing - caller subscriber will take care of it.
        messages.subscribe();

        return new IterableStream<>(tracedMessages);
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
     *
     * @see <a href="https://aka.ms/azsdk/java/servicebus/sync-receive/prefetch">Synchronous receive and prefetch</a>
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
     * @throws NullPointerException if {@code maxWaitTime} is null.
     * @throws ServiceBusException if an error occurs while receiving messages.
     * @see <a href="https://aka.ms/azsdk/java/servicebus/sync-receive/prefetch">Synchronous receive and prefetch</a>
     */
    public IterableStream<ServiceBusReceivedMessage> receiveMessages(int maxMessages, Duration maxWaitTime) {
        if (maxMessages <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'maxMessages' cannot be less than or equal to 0. maxMessages: " + maxMessages));
        } else if (Objects.isNull(maxWaitTime)) {
            throw LOGGER.logExceptionAsError(
                new NullPointerException("'maxWaitTime' cannot be null."));
        } else if (maxWaitTime.isNegative() || maxWaitTime.isZero()) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maxWaitTime' cannot be zero or less. maxWaitTime: " + maxWaitTime));
        }

        // There are two subscribers to this emitter. One is the timeout between messages subscription in
        // SynchronousReceiverWork.start() and the other is the IterableStream(emitter.asFlux());
        // Since the subscriptions may happen at different times, we want to replay results to downstream subscribers.
        final Sinks.Many<ServiceBusReceivedMessage> emitter = Sinks.many().replay().all();

        queueWork(maxMessages, maxWaitTime, emitter);

        final Flux<ServiceBusReceivedMessage> messagesFlux = emitter.asFlux();
        final Flux<ServiceBusReceivedMessage> tracedMessages = tracer.traceSyncReceive("ServiceBus.receiveMessages", messagesFlux);

        // Subscribe to message flux so we can kick off this operation, but not to tracing - caller subscriber will take care of it.
        messagesFlux.subscribe();

        return new IterableStream<>(tracedMessages);
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
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred message cannot be received.
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
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws ServiceBusException if deferred message cannot be received.
     */
    IterableStream<ServiceBusReceivedMessage> receiveDeferredMessageBatch(Iterable<Long> sequenceNumbers,
        String sessionId) {

        final Flux<ServiceBusReceivedMessage> messages = asyncClient.receiveDeferredMessages(sequenceNumbers,
            sessionId).timeout(operationTimeout);

        final Flux<ServiceBusReceivedMessage> tracedMessages = tracer.traceSyncReceive("ServiceBus.receiveDeferredMessageBatch", messages);
        // Subscribe so we can kick off this operation.
        messages.subscribe();

        return new IterableStream<>(tracedMessages);
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
     * @throws NullPointerException if {@code message}, {@code message.getLockToken()}, or
     *      {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a session receiver or the receiver is disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     * @throws ServiceBusException If the message cannot be renewed.
     */
    public void renewMessageLock(ServiceBusReceivedMessage message, Duration maxLockRenewalDuration,
        Consumer<Throwable> onError) {
        final String lockToken = message != null ? message.getLockToken() : "null";
        final Consumer<Throwable> throwableConsumer = onError != null
            ? onError
            : error -> LOGGER.atWarning().addKeyValue(LOCK_TOKEN_KEY, lockToken).log("Exception occurred while renewing lock token.", error);

        asyncClient.renewMessageLock(message, maxLockRenewalDuration).subscribe(
            v -> LOGGER.atVerbose().addKeyValue(LOCK_TOKEN_KEY, lockToken).log("Completed renewing lock token."),
            throwableConsumer,
            () -> LOGGER.atVerbose().addKeyValue(LOCK_TOKEN_KEY, lockToken).log("Auto message lock renewal operation completed"));
    }

    /**
     * Sets the state of the session if this receiver is a session receiver.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session lock cannot be renewed.
     */
    public OffsetDateTime renewSessionLock() {
        return asyncClient.renewSessionLock(asyncClient.getReceiverOptions().getSessionId()).block(operationTimeout);
    }

    /**
     * Starts the auto lock renewal for the session that this receiver works for.
     *
     * @param maxLockRenewalDuration Maximum duration to keep renewing the session.
     * @param onError A function to call when an error occurs during lock renewal.
     *
     * @throws NullPointerException if {@code sessionId} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalArgumentException if {@code sessionId} is an empty string or {@code maxLockRenewalDuration} is negative.
     * @throws IllegalStateException if the receiver is a non-session receiver or the receiver is disposed.
     * @throws ServiceBusException if the session lock renewal operation cannot be started.
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
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * ServiceBusTransactionContext transaction = receiver.createTransaction&#40;&#41;;
     *
     * &#47;&#47; Process messages and associate operations with the transaction.
     * ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage&#40;sequenceNumber&#41;;
     * receiver.complete&#40;deferredMessage, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.commitTransaction&#40;transaction&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
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
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * ServiceBusTransactionContext transaction = receiver.createTransaction&#40;&#41;;
     *
     * &#47;&#47; Process messages and associate operations with the transaction.
     * ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage&#40;sequenceNumber&#41;;
     * receiver.complete&#40;deferredMessage, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.commitTransaction&#40;transaction&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
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
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * ServiceBusTransactionContext transaction = receiver.createTransaction&#40;&#41;;
     *
     * &#47;&#47; Process messages and associate operations with the transaction.
     * ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage&#40;sequenceNumber&#41;;
     * receiver.complete&#40;deferredMessage, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;;
     * receiver.commitTransaction&#40;transaction&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext -->
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
        Sinks.Many<ServiceBusReceivedMessage> emitter) {

        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maxWaitTime, emitter);
        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.get();

        if (messageSubscriber != null) {
            messageSubscriber.queueWork(work);
            return;
        }

        messageSubscriber = synchronousMessageSubscriber.updateAndGet(subscriber -> {
            // Ensuring we create SynchronousMessageSubscriber only once.
            if (subscriber == null) {
                return new SynchronousMessageSubscriber(asyncClient,
                    work,
                    isPrefetchDisabled,
                    operationTimeout);
            } else {
                return subscriber;
            }
        });

        // NOTE: We asynchronously send the credit to the service as soon as receiveMessage() API is called (for first
        // time).
        // This means that there may be messages internally buffered before users start iterating the IterableStream.
        // If users do not iterate through the stream and their lock duration expires, it is possible that the
        // Service Bus message's delivery count will be incremented.
        if (!syncSubscribed.getAndSet(true)) {
            // The 'subscribeWith' has side effects hence must not be called from
            // the above updateFunction of AtomicReference::updateAndGet.
            asyncClient.receiveMessagesNoBackPressure().subscribeWith(messageSubscriber);
        } else {
            messageSubscriber.queueWork(work);
        }

        LOGGER.atVerbose()
            .addKeyValue(WORK_ID_KEY, work.getId())
            .log("Receive request queued up.");
    }

    void renewSessionLock(String sessionId, Duration maxLockRenewalDuration, Consumer<Throwable> onError) {
        final Consumer<Throwable> throwableConsumer = onError != null
            ? onError
            : error -> LOGGER.atWarning().addKeyValue(SESSION_ID_KEY, sessionId).log("Exception occurred while renewing session.", error);

        asyncClient.renewSessionLock(maxLockRenewalDuration).subscribe(
            v -> LOGGER.atVerbose().addKeyValue(SESSION_ID_KEY, sessionId).log("Completed renewing session"),
            throwableConsumer,
            () -> LOGGER.atVerbose().addKeyValue(SESSION_ID_KEY, sessionId).log("Auto session lock renewal operation completed."));
    }

    void setSessionState(String sessionId, byte[] sessionState) {
        asyncClient.setSessionState(sessionId, sessionState).block(operationTimeout);
    }

    byte[] getSessionState(String sessionId) {
        return asyncClient.getSessionState(sessionId).block(operationTimeout);
    }
}
