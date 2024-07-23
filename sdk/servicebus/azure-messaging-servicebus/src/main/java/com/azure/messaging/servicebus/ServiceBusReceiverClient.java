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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.WORK_ID_KEY;

/**
 * A <b>synchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a queue or
 * topic/subscription on Azure Service Bus.
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Create a receiver and receive messages</strong></p>
 *
 * <p>The following code sample demonstrates the creation and use of the synchronous client
 * {@link com.azure.messaging.servicebus.ServiceBusReceiverClient} to receive messages from a Service Bus subscription.
 * The receive operation returns when either 10 messages are received or 30 seconds has elapsed.  By default, messages
 * are received using {@link com.azure.messaging.servicebus.models.ServiceBusReceiveMode#PEEK_LOCK} and customers must
 * settle their messages using one of the settlement methods on the receiver client.
 * "<a href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">
 *     "Settling receive operations</a>" provides additional information about message settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration -->
 * <pre>
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusReceiverClient receiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .receiver&#40;&#41;
 *     .topicName&#40;topicName&#41;
 *     .subscriptionName&#40;subscriptionName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Receives a batch of messages when 10 messages are received or until 30 seconds have elapsed, whichever
 * &#47;&#47; happens first.
 * IterableStream&lt;ServiceBusReceivedMessage&gt; messages = receiver.receiveMessages&#40;10, Duration.ofSeconds&#40;30&#41;&#41;;
 * messages.forEach&#40;message -&gt; &#123;
 *     System.out.printf&#40;&quot;Id: %s. Contents: %s%n&quot;, message.getMessageId&#40;&#41;, message.getBody&#40;&#41;&#41;;
 *
 *     &#47;&#47; If able to process message, complete it. Otherwise, abandon it and allow it to be
 *     &#47;&#47; redelivered.
 *     if &#40;isMessageProcessed&#41; &#123;
 *         receiver.complete&#40;message&#41;;
 *     &#125; else &#123;
 *         receiver.abandon&#40;message&#41;;
 *     &#125;
 * &#125;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, dispose of the receiver.
 * &#47;&#47; Clients should be long-lived objects as they
 * &#47;&#47; require resources and time to establish a connection to the service.
 * receiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration -->
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
    private final ReentrantLock createSubscriberLock = new ReentrantLock();
    private final boolean isV2;
    private final SynchronousReceiver syncReceiver;

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
        // asyncClient.isV2() true indicates that the user chose v2 "Sync Receive", so this backing asyncClient was
        // also built to use v2 stack.
        this.isV2 = asyncClient.isV2();
        this.syncReceiver = new SynchronousReceiver(LOGGER, asyncClient);
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
        return asyncClient.getSessionState().block(operationTimeout);
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

        final Flux<ServiceBusReceivedMessage> messages = tracer.traceSyncReceive("ServiceBus.peekMessages",
            asyncClient.peekMessages(maxMessages, sessionId).timeout(operationTimeout));

        return fromFluxAndSubscribe(messages);
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

        final Flux<ServiceBusReceivedMessage> messages = tracer.traceSyncReceive("ServiceBus.peekMessages",
                asyncClient.peekMessages(maxMessages, sequenceNumber, sessionId).timeout(operationTimeout));

        return fromFluxAndSubscribe(messages);
    }

    /**
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity. The
     * receive operation will wait for a default 1 minute for receiving a message before it times out. You can
     * override it by using {@link #receiveMessages(int, Duration)}.
     * <p>
     * The 1-minute timeout is a client-side feature. Each time the application calls {@code receiveMessages}, a timer
     * is started on the client that when expires will terminate the IterableStream returned from this method. Timeout
     * being a client-side feature means it is impossible to cancel any message requests that already made it to the broker.
     * The messages can still arrive in the background after the IterableStream is transitioned to terminated state due
     * to the client-side timeout. If there is no active IterableStream, the client will attempt to release any buffered
     * messages back to the broker to avoid messages from going to dead letter. While messages are being released, if a
     * new active IterableStream appears (due to a new {@code receiveMessages} call) then client will stop further release,
     * so application may receive some messages from the buffer or already in transit followed by previously released
     * messages when broker redeliver them, which can appear as out of order delivery.
     * </p>
     * <p>
     * To keep the lock on each message received from a non-session resource (queue, topic subscription), the client will
     * run a background task that will continuously renew the lock before it expires. By default, the lock renew task will
     * run for a duration of 5 minutes, this duration can be adjusted using
     * the {@link ServiceBusReceiverClientBuilder#maxAutoLockRenewDuration(Duration)} API or can be turned off by
     * setting it {@link Duration#ZERO}. A higher {@code maxMessages} value means an equivalent number of lock renewal
     * tasks running in the client, which may put more stress on low CPU environments. Given each lock renewal is a network
     * call to the broker, a high number of lock renewal tasks making multiple lock renew calls also may have an adverse
     * effect in namespace throttling. Additionally, if certain lock renewal tasks fail to renew the lock on time because
     * of low CPU, service throttling or overloaded network, then client may lose the lock on the messages, which will
     * cause the application's attempts to settle (e.g., complete, abandon) those messages to fail. The broker will
     * redeliver those messages, but if the settling attempts fail repeatedly beyond the max delivery count, then the message
     * will be transferred to dead letter queue. Keep this in mind when choosing {@code maxMessages}. You may consider
     * disabling the client-side lock renewal using {@code maxAutoLockRenewDuration(Duration.ZERO)} if you can configure
     * a lock duration at the resource (queue,topic subscription) level that at least exceeds the cumulative expected
     * processing time for {@code maxMessages} messages.
     * </p>
     * <p>
     * The client uses an AMQP link underneath to receive the messages; the client will transparently transition
     * to a new AMQP link if the current one encounters a retriable error. When the client experiences a non-retriable
     * error or exhausts the retries, the iteration (e.g., forEach) on the {@link IterableStream} returned by the further
     * invocations of receiveMessages API will throw the error to the application. Once the application receives
     * this error, the application should reset the client, i.e., close the current {@link ServiceBusReceiverClient}
     * and create a new client to continue receiving messages.
     * </p>
     * <p>
     * Note: A few examples of non-retriable errors are - the application attempting to connect to a queue that does not
     * exist, deleting or disabling the queue in the middle of receiving, the user explicitly initiating Geo-DR.
     * These are certain events where the Service Bus communicates to the client that a non-retriable error occurred.
     * </p>
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
     * Receives an iterable stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity with a timout.
     * The default receive mode is {@link ServiceBusReceiveMode#PEEK_LOCK } unless it is changed during  creation of
     * {@link ServiceBusReceiverClient} using {@link ServiceBusReceiverClientBuilder#receiveMode(ServiceBusReceiveMode)}.
     * <p>
     * The support for timeout {@code maxWaitTime} is a client-side feature. Each time the application calls
     * {@code receiveMessages}, a timer is started on the client that when expires will terminate the IterableStream
     * returned from this method. Timeout being a client-side feature means it is impossible to cancel any message
     * requests that already made it to the broker. The messages can still arrive in the background after
     * the IterableStream is transitioned to terminated state due to the client-side timeout. If there is no active
     * IterableStream, the client will attempt to release any buffered messages back to the broker to avoid messages from
     * going to dead letter. While messages are being released, if a new active IterableStream appears (due to a new
     * {@code receiveMessages} call) then client will stop further release, so application may receive some messages from
     * the buffer or already in transit followed by previously released messages when broker redeliver them, which can
     * appear as out of order delivery. Consider these when choosing the timeout. For example, a small timeout with
     * a higher {@code maxMessages} value while there are a lot of messages in the entity can increase the release
     * network calls to the broker that might have adverse effect in namespace throttling and increases the chances of
     * out of order deliveries. Also, frequent receiveMessages with low timeout means frequent scheduling of timer tasks,
     * which may put more stress on low CPU environments.
     * </p>
     * <p>
     * To keep the lock on each message received from a non-session resource (queue, topic subscription), the client will
     * run a background task that will continuously renew the lock before it expires. By default, the lock renew task will
     * run for a duration of 5 minutes, this duration can be adjusted using
     * the {@link ServiceBusReceiverClientBuilder#maxAutoLockRenewDuration(Duration)} API or can be turned off by
     * setting it {@link Duration#ZERO}. A higher {@code maxMessages} value means an equivalent number of lock renewal
     * tasks running in the client, which may put more stress on low CPU environments. Given each lock renewal is a network
     * call to the broker, a high number of lock renewal tasks making multiple lock renew calls also may have an adverse
     * effect in namespace throttling. Additionally, if certain lock renewal tasks fail to renew the lock on time because
     * of low CPU, service throttling or overloaded network, then client may lose the lock on the messages, which will
     * cause the application's attempts to settle (e.g., complete, abandon) those messages to fail. The broker will
     * redeliver those messages, but if the settling attempts fail repeatedly beyond the max delivery count, then the message
     * will be transferred to dead letter queue. Keep this in mind when choosing {@code maxMessages}. You may consider
     * disabling the client-side lock renewal using {@code maxAutoLockRenewDuration(Duration.ZERO)} if you can configure
     * a lock duration at the resource (queue,topic subscription) level that at least exceeds the cumulative expected
     * processing time for {@code maxMessages} messages.
     * </p>
     * <p>
     * The client uses an AMQP link underneath to receive the messages; the client will transparently transition
     * to a new AMQP link if the current one encounters a retriable error. When the client experiences a non-retriable
     * error or exhausts the retries, the iteration (e.g., forEach) on the {@link IterableStream} returned by the further
     * invocations of receiveMessages API will throw the error to the application. Once the application receives
     * this error, the application should reset the client, i.e., close the current {@link ServiceBusReceiverClient}
     * and create a new client to continue receiving messages.
     * </p>
     * <p>
     * Note: A few examples of non-retriable errors are - the application attempting to connect to a queue that does not
     * exist, deleting or disabling the queue in the middle of receiving, the user explicitly initiating Geo-DR.
     * These are certain events where the Service Bus communicates to the client that a non-retriable error occurred.
     * </p>
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

        if (isV2) {
            return syncReceiver.receive(maxMessages, maxWaitTime);
        }

        // V1: queue the work and return IterableStream backed by the work.
        //
        // There are two subscribers to this emitter. One is the timeout between messages subscription in
        // SynchronousReceiverWork.start() and the other is the IterableStream(emitter.asFlux());
        // Since the subscriptions may happen at different times, we want to replay results to downstream subscribers.
        final Sinks.Many<ServiceBusReceivedMessage> emitter = Sinks.many().replay().all();

        queueWork(maxMessages, maxWaitTime, emitter);

        final Flux<ServiceBusReceivedMessage> messagesFlux =  tracer.traceSyncReceive("ServiceBus.receiveMessages", emitter.asFlux());
        // messagesFlux is already a hot publisher, so it's ok to subscribe
        messagesFlux.subscribe();

        return new IterableStream<>(messagesFlux);
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

        final Flux<ServiceBusReceivedMessage> messages = tracer.traceSyncReceive("ServiceBus.receiveDeferredMessageBatch",
            asyncClient.receiveDeferredMessages(sequenceNumbers, sessionId).timeout(operationTimeout));

        return fromFluxAndSubscribe(messages);
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
        return asyncClient.renewSessionLock().block(operationTimeout);
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
        asyncClient.setSessionState(sessionState).block(operationTimeout);
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along to all
     * operations that need to be in this transaction.
     *
     * <p><strong>Sample: Creating and using a transaction</strong></p>
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
     * @throws IllegalStateException if the receiver is already disposed.
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
        if (isV2) {
            syncReceiver.dispose();
        }
        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.get();
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
        assert !isV2;

        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maxWaitTime, emitter);
        SynchronousMessageSubscriber messageSubscriber = synchronousMessageSubscriber.get();

        if (messageSubscriber != null) {
            messageSubscriber.queueWork(work);
            LOGGER.atVerbose().addKeyValue(WORK_ID_KEY, work.getId()).log("Receive request queued up.");
            return;
        }

        final boolean isFirstWork;
        createSubscriberLock.lock();
        try {
            messageSubscriber = synchronousMessageSubscriber.get();
            isFirstWork = messageSubscriber == null;
            if (isFirstWork) {
                messageSubscriber = new SynchronousMessageSubscriber(asyncClient,
                    work,
                    isPrefetchDisabled,
                    operationTimeout);
                synchronousMessageSubscriber.set(messageSubscriber);
            }
        } finally {
            createSubscriberLock.unlock();
        }

        // NOTE: We asynchronously send the credit to the service as soon as receiveMessage() API is called (for first
        // time).
        // This means that there may be messages internally buffered before users start iterating the IterableStream.
        // If users do not iterate through the stream and their lock duration expires, it is possible that the
        // Service Bus message's delivery count will be incremented.
        if (isFirstWork) {
            LOGGER.atVerbose().addKeyValue(WORK_ID_KEY, work.getId()).log("Receive request queued up.");
            // The 'subscribeWith' has side effects hence must not be called from the above block synchronized using 'createSubscriberLock'.
            asyncClient.receiveMessagesNoBackPressure().subscribeWith(messageSubscriber);
        } else {
            messageSubscriber.queueWork(work);
            LOGGER.atVerbose().addKeyValue(WORK_ID_KEY, work.getId()).log("Receive request queued up.");
        }
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

    private <T> IterableStream<T> fromFluxAndSubscribe(Flux<T> flux)  {
        Flux<T> cached = flux.cache();

        // Subscribe to message flux so we can kick off this operation
        cached.subscribe();
        return new IterableStream<>(cached);
    }
}
