// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This <b>synchronous</b> session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverClient} instances that are tied to the locked sessions.  Sessions can be used as a first in
 * first out (FIFO) processing of messages.  Queues and topics/subscriptions support Service Bus sessions, however, it
 * must be <a href="https://learn.microsoft.com/azure/service-bus-messaging/enable-message-sessions">enabled at the time
 *      of entity creation</a>.
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
 * <p><strong>Sample: Receive messages from a specific session</strong></p>
 *
 * <p>Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.
 * {@link ServiceBusReceiveMode#PEEK_LOCK} is <strong>strongly</strong> recommended so users have control over message
 * settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildClient&#40;&#41;;
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptSession&#40;&quot;&lt;&lt;my-session-id&gt;&gt;&quot;&#41;;
 *
 * &#47;&#47; Keep fetching messages from the session until there are no more messages.
 * &#47;&#47; The receiveMessage operation returns when either 10 messages have been receiver or, 30 seconds have elapsed.
 * boolean hasMoreMessages = true;
 * while &#40;hasMoreMessages&#41; &#123;
 *     IterableStream&lt;ServiceBusReceivedMessage&gt; messages =
 *         receiver.receiveMessages&#40;10, Duration.ofSeconds&#40;30&#41;&#41;;
 *     Iterator&lt;ServiceBusReceivedMessage&gt; iterator = messages.iterator&#40;&#41;;
 *     hasMoreMessages = iterator.hasNext&#40;&#41;;
 *
 *     while &#40;iterator.hasNext&#40;&#41;&#41; &#123;
 *         ServiceBusReceivedMessage message = iterator.next&#40;&#41;;
 *         System.out.printf&#40;&quot;Session Id: %s. Contents: %s%n.&quot;, message.getSessionId&#40;&#41;, message.getBody&#40;&#41;&#41;;
 *
 *         &#47;&#47; Explicitly settle the message using complete, abandon, defer, dead-letter, etc.
 *         if &#40;isMessageProcessed&#41; &#123;
 *             receiver.complete&#40;message&#41;;
 *         &#125; else &#123;
 *             receiver.abandon&#40;message&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; Use the receiver and finally close it along with the sessionReceiver.
 * receiver.close&#40;&#41;;
 * sessionReceiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId -->
 *
 * <p><strong>Sample: Receive messages from the first available session</strong></p>
 * <p>Use {@link #acceptNextSession()} to acquire the lock of the next available session without specifying the session
 * id.  {@link ServiceBusReceiveMode#PEEK_LOCK} is <strong>strongly</strong> recommended so users have control over
 * message settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Creates a client to receive messages from the first available session. It waits until
 * &#47;&#47; AmqpRetryOptions.getTryTimeout&#40;&#41; elapses. If no session is available within that operation timeout, it
 * &#47;&#47; throws a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession&#40;&#41;;
 *
 * &#47;&#47; Use the receiver and finally close it along with the sessionReceiver.
 * try &#123;
 *     &#47;&#47; Keep fetching messages from the session until there are no more messages.
 *     &#47;&#47; The receiveMessage operation returns when either 10 messages have been receiver or, 30 seconds have elapsed.
 *     boolean hasMoreMessages = true;
 *     while &#40;hasMoreMessages&#41; &#123;
 *         IterableStream&lt;ServiceBusReceivedMessage&gt; messages =
 *             receiver.receiveMessages&#40;10, Duration.ofSeconds&#40;30&#41;&#41;;
 *         Iterator&lt;ServiceBusReceivedMessage&gt; iterator = messages.iterator&#40;&#41;;
 *         hasMoreMessages = iterator.hasNext&#40;&#41;;
 *
 *         while &#40;iterator.hasNext&#40;&#41;&#41; &#123;
 *             ServiceBusReceivedMessage message = iterator.next&#40;&#41;;
 *             System.out.printf&#40;&quot;Session Id: %s. Message: %s%n.&quot;, message.getSessionId&#40;&#41;, message.getBody&#40;&#41;&#41;;
 *
 *             &#47;&#47; Explicitly settle the message using complete, abandon, defer, dead-letter, etc.
 *             if &#40;isMessageProcessed&#41; &#123;
 *                 receiver.complete&#40;message&#41;;
 *             &#125; else &#123;
 *                 receiver.abandon&#40;message&#41;;
 *             &#125;
 *         &#125;
 *     &#125;
 * &#125; finally &#123;
 *     receiver.close&#40;&#41;;
 *     sessionReceiver.close&#40;&#41;;
 * &#125;
 *
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession -->
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusSessionReceiverClient implements AutoCloseable {
    private static final String TIMEOUT_MESSAGE_PREFIX = "Timeout on blocking read for ";
    private final ServiceBusSessionReceiverAsyncClient sessionAsyncClient;
    private final boolean isPrefetchDisabled;
    private final Duration operationTimeout;

    ServiceBusSessionReceiverClient(ServiceBusSessionReceiverAsyncClient asyncClient, boolean isPrefetchDisabled,
        Duration operationTimeout) {
        this.sessionAsyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = operationTimeout;
        this.isPrefetchDisabled = isPrefetchDisabled;
    }

    /**
     * Acquires a session lock for the next available session and creates a {@link ServiceBusReceiverClient}
     * to receive messages from the session. If no session is available immediately, it waits for one; if none
     * becomes available within the operation timeout it throws a retriable timeout error rather than blocking
     * indefinitely, so callers typically invoke this in a loop.
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     *
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws IllegalStateException if no session becomes available within the operation timeout (the total
     *      of all retry attempts derived from {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)});
     *      this is retriable, so callers typically retry.
     * @throws AmqpException if acquiring the session fails at the AMQP level (for example an authorization or
     *      connection error surfaced by the underlying asynchronous accept).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptNextSession() {
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, isPrefetchDisabled, operationTimeout))
            .timeout(operationTimeout, Mono.error(() -> {
                final String message = TIMEOUT_MESSAGE_PREFIX + operationTimeout.toNanos() + " " + TimeUnit.NANOSECONDS
                    + " (client-timeout)";
                return new TimeoutException(message);
            }))
            .onErrorMap(TimeoutException.class, e -> new IllegalStateException(e.getMessage(), e))
            .block();
    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session. If the session is already locked by another client, an
     * {@link AmqpException} is thrown immediately.
     *
     * @param sessionId The session id.
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     *
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws ServiceBusException if the lock cannot be acquired.
     * @throws IllegalStateException if the session is not acquired within the operation timeout (the total
     *      of all retry attempts derived from {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)});
     *      this is retriable, so callers typically retry.
     * @throws AmqpException if acquiring the session fails at the AMQP level (for example the session is already
     *      locked by another client, or an authorization or connection error surfaced by the underlying
     *      asynchronous accept).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptSession(String sessionId) {
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, isPrefetchDisabled, operationTimeout))
            .timeout(operationTimeout, Mono.error(() -> {
                final String message = TIMEOUT_MESSAGE_PREFIX + operationTimeout.toNanos() + " " + TimeUnit.NANOSECONDS
                    + " (client-timeout)";
                return new TimeoutException(message);
            }))
            .onErrorMap(TimeoutException.class, e -> new IllegalStateException(e.getMessage(), e))
            .block();
    }

    /**
     * Lists the IDs of sessions that have active messages in this entity.
     *
     * <p>The returned {@link PagedIterable} fetches additional pages from the broker on demand;
     * iterate the {@code PagedIterable} (or call {@link PagedIterable#stream()}) to receive every
     * session ID. Pages are fetched lazily as the iterator advances. The default page size is 100;
     * callers can request a different size via {@link PagedIterable#iterableByPage(int)} (or the
     * equivalent on the underlying {@code PagedFlux}).</p>
     *
     * @return A {@link PagedIterable} of session ID strings.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSessions() {
        return new PagedIterable<>(sessionAsyncClient.listSessions());
    }

    /**
     * Lists the IDs of sessions whose state was updated after the specified time.
     *
     * <p>The returned {@link PagedIterable} fetches additional pages from the broker on demand;
     * iterate the {@code PagedIterable} (or call {@link PagedIterable#stream()}) to receive every
     * session ID. Pages are fetched lazily as the iterator advances. The default page size is 100;
     * callers can request a different size via {@link PagedIterable#iterableByPage(int)} (or the
     * equivalent on the underlying {@code PagedFlux}).</p>
     *
     * <p>Values at or beyond the active-messages sentinel value
     * ({@code new Date(253402300800000L)}, rendered by {@code OffsetDateTime.toString()} as
     * {@code +10000-01-01T00:00Z}, matching Track 1's {@code SessionBrowser.MAXDATE}) are clamped
     * to that sentinel and behave the same as {@link #listSessions()}, returning sessions that
     * have active messages.</p>
     *
     * @param sessionStateUpdatedAfter Only sessions whose session state was updated after this time are returned.
     * @return A {@link PagedIterable} of session ID strings.
     * @throws NullPointerException if {@code sessionStateUpdatedAfter} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listSessions(OffsetDateTime sessionStateUpdatedAfter) {
        Objects.requireNonNull(sessionStateUpdatedAfter, "'sessionStateUpdatedAfter' cannot be null.");
        return new PagedIterable<>(sessionAsyncClient.listSessions(sessionStateUpdatedAfter));
    }

    @Override
    public void close() {
        sessionAsyncClient.close();
    }
}
