// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannelClosedException;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusSenderInstrumentation;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_SENDER;

/**
 * <p>An <b>asynchronous</b> client to send messages to a Service Bus resource.</p>
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
 * <p><strong>Sample: Create an instance of sender</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderAsyncClient asyncSender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; When users are done with the sender, they should dispose of it.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * asyncSender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation -->
 *
 * <p><strong>Sample: Send messages to a Service Bus resource</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch -->
 * <pre>
 * &#47;&#47; `subscribe` is a non-blocking call. The program will move onto the next line of code when it starts the
 * &#47;&#47; operation.  Users should use the callbacks on `subscribe` to understand the status of the send operation.
 * asyncSender.createMessageBatch&#40;&#41;.flatMap&#40;batch -&gt; &#123;
 *     batch.tryAddMessage&#40;new ServiceBusMessage&#40;&quot;test-1&quot;&#41;&#41;;
 *     batch.tryAddMessage&#40;new ServiceBusMessage&#40;&quot;test-2&quot;&#41;&#41;;
 *     return asyncSender.sendMessages&#40;batch&#41;;
 * &#125;&#41;.subscribe&#40;unused -&gt; &#123;
 * &#125;, error -&gt; &#123;
 *     System.err.println&#40;&quot;Error occurred while sending batch:&quot; + error&#41;;
 * &#125;, &#40;&#41; -&gt; &#123;
 *     System.out.println&#40;&quot;Send complete.&quot;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch -->
 *
 * <p><strong>Sample: Send messages using a size-limited {@link ServiceBusMessageBatch} to a Service Bus resource
 * </strong></p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch#CreateMessageBatchOptionsLimitedSize -->
 * <pre>
 * Flux&lt;ServiceBusMessage&gt; telemetryMessages = Flux.just&#40;firstMessage, secondMessage&#41;;
 *
 * &#47;&#47; Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
 * &#47;&#47; In this case, all the batches created with these options are limited to 256 bytes.
 * CreateMessageBatchOptions options = new CreateMessageBatchOptions&#40;&#41;
 *     .setMaximumSizeInBytes&#40;256&#41;;
 * AtomicReference&lt;ServiceBusMessageBatch&gt; currentBatch = new AtomicReference&lt;&gt;&#40;&#41;;
 *
 * &#47;&#47; Sends the current batch if it is not null and not empty.  If the current batch is null, sets it.
 * &#47;&#47; Returns the batch to work with.
 * Mono&lt;ServiceBusMessageBatch&gt; sendBatchAndGetCurrentBatchOperation = Mono.defer&#40;&#40;&#41; -&gt; &#123;
 *     ServiceBusMessageBatch batch = currentBatch.get&#40;&#41;;
 *
 *     if &#40;batch == null&#41; &#123;
 *         return asyncSender.createMessageBatch&#40;options&#41;;
 *     &#125;
 *
 *     if &#40;batch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *         return asyncSender.sendMessages&#40;batch&#41;.then&#40;
 *             asyncSender.createMessageBatch&#40;options&#41;
 *                 .handle&#40;&#40;ServiceBusMessageBatch newBatch, SynchronousSink&lt;ServiceBusMessageBatch&gt; sink&#41; -&gt; &#123;
 *                     &#47;&#47; Expect that the batch we just sent is the current one. If it is not, there's a race
 *                     &#47;&#47; condition accessing currentBatch reference.
 *                     if &#40;!currentBatch.compareAndSet&#40;batch, newBatch&#41;&#41; &#123;
 *                         sink.error&#40;new IllegalStateException&#40;
 *                             &quot;Expected that the object in currentBatch was batch. But it is not.&quot;&#41;&#41;;
 *                     &#125; else &#123;
 *                         sink.next&#40;newBatch&#41;;
 *                     &#125;
 *                 &#125;&#41;&#41;;
 *     &#125; else &#123;
 *         return Mono.just&#40;batch&#41;;
 *     &#125;
 * &#125;&#41;;
 *
 * &#47;&#47; The sample Flux contains two messages, but it could be an infinite stream of telemetry messages.
 * Flux&lt;Void&gt; sendMessagesOperation = telemetryMessages.flatMap&#40;message -&gt; &#123;
 *     return sendBatchAndGetCurrentBatchOperation.flatMap&#40;batch -&gt; &#123;
 *         if &#40;batch.tryAddMessage&#40;message&#41;&#41; &#123;
 *             return Mono.empty&#40;&#41;;
 *         &#125; else &#123;
 *             return sendBatchAndGetCurrentBatchOperation
 *                 .handle&#40;&#40;ServiceBusMessageBatch newBatch, SynchronousSink&lt;Void&gt; sink&#41; -&gt; &#123;
 *                     if &#40;!newBatch.tryAddMessage&#40;message&#41;&#41; &#123;
 *                         sink.error&#40;new IllegalArgumentException&#40;
 *                             &quot;Message is too large to fit in an empty batch.&quot;&#41;&#41;;
 *                     &#125; else &#123;
 *                         sink.complete&#40;&#41;;
 *                     &#125;
 *                 &#125;&#41;;
 *         &#125;
 *     &#125;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; `subscribe` is a non-blocking call. The program will move onto the next line of code when it starts the
 * &#47;&#47; operation.  Users should use the callbacks on `subscribe` to understand the status of the send operation.
 * Disposable disposable = sendMessagesOperation.then&#40;sendBatchAndGetCurrentBatchOperation&#41;
 *     .subscribe&#40;batch -&gt; &#123;
 *         System.out.println&#40;&quot;Last batch should be empty: &quot; + batch.getCount&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error sending telemetry messages: &quot; + error&#41;;
 *     &#125;, &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Completed.&quot;&#41;;
 *
 *         &#47;&#47; Continue using the sender and finally, dispose of the sender.
 *         &#47;&#47; Clients should be long-lived objects as they require resources
 *         &#47;&#47; and time to establish a connection to the service.
 *         asyncSender.close&#40;&#41;;
 *     &#125;&#41;;
 *
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch#CreateMessageBatchOptionsLimitedSize -->
 *
 * <p><strong>Sample: Sending a message to a session-enabled queue</strong></p>
 *
 * <p>The snippet below demonstrates sending a message to a
 * <a href="https://learn.microsoft.com/azure/service-bus-messaging/message-sessions">Service Bus sessions</a>
 * enabled queue.  Setting {@link ServiceBusMessage#setMessageId(String)} property to "greetings" will send the message
 * to a Service Bus session with an id of "greetings".</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderasyncclient.sendMessage-session -->
 * <pre>
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; Setting sessionId publishes that message to a specific session, in this case, &quot;greeting&quot;.
 * ServiceBusMessage message = new ServiceBusMessage&#40;&quot;Hello world&quot;&#41;
 *     .setSessionId&#40;&quot;greetings&quot;&#41;;
 *
 * &#47;&#47; `subscribe` is a non-blocking call. The program will move onto the next line of code when it starts the
 * &#47;&#47; operation.  Users should use the callbacks on `subscribe` to understand the status of the send operation.
 * sender.sendMessage&#40;message&#41;.subscribe&#40;unused -&gt; &#123;
 * &#125;, error -&gt; &#123;
 *     System.err.println&#40;&quot;Error occurred publishing batch: &quot; + error&#41;;
 * &#125;, &#40;&#41; -&gt; &#123;
 *     System.out.println&#40;&quot;Send complete.&quot;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; Continue using the sender and finally, dispose of the sender.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * sender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderasyncclient.sendMessage-session -->
 *
 * @see ServiceBusClientBuilder#sender()
 * @see ServiceBusSenderClient To communicate with a Service Bus resource using a synchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusSenderAsyncClient implements AutoCloseable {
    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;
    private static final String TRANSACTION_LINK_NAME = "coordinator";
    private static final ServiceBusMessage END = new ServiceBusMessage(new byte[0]);
    private static final CreateMessageBatchOptions DEFAULT_BATCH_OPTIONS =  new CreateMessageBatchOptions();

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSenderAsyncClient.class);
    private final AtomicReference<String> linkName = new AtomicReference<>();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final MessageSerializer messageSerializer;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final MessagingEntityType entityType;
    private final Runnable onClientClose;
    private final String entityName;
    private final Mono<ServiceBusAmqpConnection> connectionProcessor;
    private final String fullyQualifiedNamespace;
    private final String viaEntityName;
    private final String identifier;
    private final ServiceBusSenderInstrumentation instrumentation;
    private final ServiceBusTracer tracer;
    private final boolean isV2;

    /**
     * Creates a new instance of this {@link ServiceBusSenderAsyncClient} that sends messages to a Service Bus entity.
     */
    ServiceBusSenderAsyncClient(String entityName, MessagingEntityType entityType,
        ConnectionCacheWrapper connectionCacheWrapper, AmqpRetryOptions retryOptions, ServiceBusSenderInstrumentation instrumentation,
        MessageSerializer messageSerializer, Runnable onClientClose, String viaEntityName, String identifier) {
        // Caching the created link so we don't invoke another link creation.
        this.messageSerializer = Objects.requireNonNull(messageSerializer,
            "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.entityName = Objects.requireNonNull(entityName, "'entityPath' cannot be null.");
        Objects.requireNonNull(connectionCacheWrapper, "'connectionCacheWrapper' cannot be null.");
        this.connectionProcessor = connectionCacheWrapper.getConnection();
        this.fullyQualifiedNamespace = connectionCacheWrapper.getFullyQualifiedNamespace();
        this.instrumentation = Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null.");
        this.tracer = instrumentation.getTracer();
        this.retryPolicy = getRetryPolicy(retryOptions);
        this.entityType = entityType;
        this.viaEntityName = viaEntityName;
        this.onClientClose = onClientClose;
        this.identifier = identifier;
        this.isV2 = connectionCacheWrapper.isV2();
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return entityName;
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusSenderAsyncClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusSenderAsyncClient}.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if {@code message} is larger than the maximum allowed size of a single message or
     *      the message could not be sent.
     */
    public Mono<Void> sendMessage(ServiceBusMessage message) {
        if (Objects.isNull(message)) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        }
        return sendInternal(Flux.just(message), null);
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     * @param transactionContext to be set on batch message before sending to Service Bus.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code message}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if {@code message} is larger than the maximum allowed size of a single message or
     *      the message could not be sent.
     */
    public Mono<Void> sendMessage(ServiceBusMessage message, ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return sendInternal(Flux.just(message), transactionContext);
    }

    /**
     * Sends a set of messages to a Service Bus queue or topic using a batched approach. If the size of messages
     * exceed the maximum size of a single batch, an exception will be triggered and the send will fail.
     * By default, the message size is the max amount allowed on the link.
     *
     * @param messages Messages to be sent to Service Bus queue or topic.
     * @param transactionContext to be set on batch message before sending to Service Bus.
     *
     * @return A {@link Mono} that completes when all messages have been sent to the Service Bus resource.
     *
     * @throws NullPointerException if {@code batch}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if the message could not be sent or {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     */
    public Mono<Void> sendMessages(Iterable<ServiceBusMessage> messages,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return sendIterable(messages, transactionContext);
    }

    /**
     * Sends a set of messages to a Service Bus queue or topic using a batched approach. If the size of messages exceed
     * the maximum size of a single batch, an exception will be triggered and the send will fail. By default, the
     * message size is the max amount allowed on the link.
     *
     * @param messages Messages to be sent to Service Bus queue or topic.
     *
     * @return A {@link Mono} that completes when all messages have been sent to the Service Bus resource.
     *
     * @throws NullPointerException if {@code messages} is {@code null}.
     * @throws ServiceBusException if the message could not be sent or {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Void> sendMessages(Iterable<ServiceBusMessage> messages) {
        return sendIterable(messages, null);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @throws ServiceBusException if the message batch could not be sent.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Void> sendMessages(ServiceBusMessageBatch batch) {
        return sendInternal(batch, null);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     * @param transactionContext to be set on batch message before sending to Service Bus.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code batch}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException if the message batch could not be sent.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Void> sendMessages(ServiceBusMessageBatch batch, ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return sendInternal(batch, transactionContext);
    }

    /**
     * Creates a {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @return A {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     * @throws ServiceBusException if the message batch could not be created.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<ServiceBusMessageBatch> createMessageBatch() {
        return createMessageBatch(DEFAULT_BATCH_OPTIONS);
    }

    /**
     * Creates an {@link ServiceBusMessageBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link ServiceBusMessageBatch}.
     *
     * @return A new {@link ServiceBusMessageBatch} configured with the given options.
     * @throws NullPointerException if {@code options} is null.
     * @throws ServiceBusException if the message batch could not be created.
     * @throws IllegalStateException if sender is already disposed.
     * @throws IllegalArgumentException if {@link CreateMessageBatchOptions#getMaximumSizeInBytes()} is larger than
     *      maximum allowed size.
     */
    public Mono<ServiceBusMessageBatch> createMessageBatch(CreateMessageBatchOptions options) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "createMessageBatch")));
        }
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        final int maxSize = options.getMaximumSizeInBytes();

        final Mono<ServiceBusMessageBatch> createBatch = getSendLink().flatMap(link -> link.getLinkSize().flatMap(size -> {
            final int maximumLinkSize = size > 0
                ? size
                : MAX_MESSAGE_LENGTH_BYTES;

            if (maxSize > maximumLinkSize) {
                return monoError(LOGGER, new IllegalArgumentException(String.format(Locale.US,
                    "CreateMessageBatchOptions.getMaximumSizeInBytes (%s bytes) is larger than the link size"
                        + " (%s bytes).", maxSize, maximumLinkSize)));
            }

            final int batchSize = maxSize > 0
                ? maxSize
                : maximumLinkSize;

            return Mono.just(new ServiceBusMessageBatch(isV2, batchSize, link::getErrorContext, tracer, messageSerializer));
        })).onErrorMap(RequestResponseChannelClosedException.class,
            e -> {
                // When the current connection is being disposed, the connectionProcessor can produce a new connection
                // if downstream request. In this context, treat RequestResponseChannelClosedException from
                // the RequestResponseChannel scoped to the current connection being disposed as retry-able so that retry
                // can obtain new connection.
                return new AmqpException(true, e.getMessage(), e, null);
            });

        // Similar to the companion API 'send', the 'create-batch' can also make network calls, so retry in case of transient errors.
        return withRetry(createBatch, retryOptions,
            String.format("entityPath[%s]: Creating batch timed out.", entityName))
            .onErrorMap(this::mapError);
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue.
     * @param scheduledEnqueueTime OffsetDateTime at which the message should appear in the Service Bus queue or topic.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message}, {@code scheduledEnqueueTime}, {@code transactionContext} or
     *      {@code transactionContext.transactionID} is {@code null}.
     * @throws ServiceBusException If the message could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Long> scheduleMessage(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return scheduleMessageInternal(message, scheduledEnqueueTime, transactionContext);
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue.
     * @param scheduledEnqueueTime OffsetDateTime at which the message should appear in the Service Bus queue or topic.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the message could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Long> scheduleMessage(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime) {
        return scheduleMessageInternal(message, scheduledEnqueueTime, null);
    }

    /**
     * Sends a batch of scheduled messages to the Azure Service Bus entity this sender is connected to. A scheduled
     * message is enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param messages Messages to be sent to the Service Bus queue or topic.
     * @param scheduledEnqueueTime OffsetDateTime at which the message should appear in the Service Bus queue or topic.
     *
     * @return Sequence numbers of the scheduled messages which can be used to cancel the messages.
     *
     * @throws NullPointerException If {@code messages} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the messages could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Flux<Long> scheduleMessages(Iterable<ServiceBusMessage> messages, OffsetDateTime scheduledEnqueueTime) {
        return scheduleMessages(messages, scheduledEnqueueTime, null);
    }

    /**
     * Sends a scheduled messages to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param messages Messages to be sent to the Service Bus Queue.
     * @param scheduledEnqueueTime OffsetDateTime at which the messages should appear in the Service Bus queue or topic.
     * @param transactionContext Transaction to associate with the operation.
     *
     * @return Sequence numbers of the scheduled messages which can be used to cancel the messages.
     *
     * @throws NullPointerException If {@code messages}, {@code scheduledEnqueueTime}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException If the messages could not be scheduled or the {@code message} is larger than
     *      the maximum size of the {@link ServiceBusMessageBatch}.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Flux<Long> scheduleMessages(Iterable<ServiceBusMessage> messages, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "scheduleMessages")));
        }
        if (Objects.isNull(messages)) {
            return fluxError(LOGGER, new NullPointerException("'messages' cannot be null."));
        }

        if (Objects.isNull(scheduledEnqueueTime)) {
            return fluxError(LOGGER, new NullPointerException("'scheduledEnqueueTime' cannot be null."));
        }

        return createMessageBatch()
            .map(messageBatch -> {
                int index = 0;
                for (ServiceBusMessage message : messages) {
                    if (!messageBatch.tryAddMessage(message)) {
                        final String error = String.format(Locale.US,
                            "Messages exceed max allowed size for all the messages together. "
                                + "Failed to add message at index '%s'.", index);
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(error));
                    }
                    ++index;
                }
                return messageBatch;
            })
            .flatMapMany(messageBatch ->
                tracer.traceScheduleFlux("ServiceBus.scheduleMessages",
                    connectionProcessor
                        .flatMap(connection -> connection.getManagementNode(entityName, entityType))
                        .flatMapMany(managementNode -> managementNode.schedule(messageBatch.getMessages(), scheduledEnqueueTime,
                            messageBatch.getMaxSizeInBytes(), linkName.get(), transactionContext)),
                    messageBatch.getMessages())
            ).onErrorMap(this::mapError);
    }

    /**
     * Cancels the enqueuing of a scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber of the scheduled message to cancel.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     *
     * @throws IllegalArgumentException if {@code sequenceNumber} is negative.
     * @throws ServiceBusException If the messages could not be cancelled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Void> cancelScheduledMessage(long sequenceNumber) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "cancelScheduledMessage")));
        }
        if (sequenceNumber < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'sequenceNumber' cannot be negative."));
        }

        return tracer.traceMono("ServiceBus.cancelScheduledMessage",
                connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityName, entityType))
                    .flatMap(managementNode -> managementNode.cancelScheduledMessages(
                        Collections.singletonList(sequenceNumber), linkName.get())))
            .onErrorMap(this::mapError);
    }

    /**
     * Cancels the enqueuing of an already scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumbers of the scheduled messages to cancel.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if the scheduled messages cannot cancelled.
     */
    public Mono<Void> cancelScheduledMessages(Iterable<Long> sequenceNumbers) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "cancelScheduledMessages")));
        }

        if (Objects.isNull(sequenceNumbers)) {
            return monoError(LOGGER, new NullPointerException("'messages' cannot be null."));
        }

        return tracer.traceMono("ServiceBus.cancelScheduledMessages",
                connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityName, entityType))
                    .flatMap(managementNode -> managementNode.cancelScheduledMessages(sequenceNumbers, linkName.get())))
            .onErrorMap(this::mapError);
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along with
     * {@link ServiceBusReceivedMessage} all operations that needs to be in this transaction.
     *
     * @return A new {@link ServiceBusTransactionContext}.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if a transaction cannot be created.
     *
     * @see ServiceBusReceiverAsyncClient#createTransaction()
     */
    public Mono<ServiceBusTransactionContext> createTransaction() {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "createTransaction")));
        }

        return tracer.traceMono("ServiceBus.createTransaction",
                connectionProcessor
                    .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                    .flatMap(transactionSession -> transactionSession.createTransaction())
                    .map(transaction -> new ServiceBusTransactionContext(transaction.getTransactionId())))
            .onErrorMap(this::mapError);
    }

    /**
     * Commits the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext to be committed.
     *
     * @return The {@link Mono} that finishes this operation on Service Bus resource.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be committed.
     *
     * @see ServiceBusReceiverAsyncClient#commitTransaction(ServiceBusTransactionContext)
     */
    public Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "commitTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return
            tracer.traceMono("ServiceBus.commitTransaction", connectionProcessor
                .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                .flatMap(transactionSession -> transactionSession.commitTransaction(new AmqpTransaction(
                    transactionContext.getTransactionId()))))
            .onErrorMap(this::mapError);
    }

    /**
     * Rollbacks the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext Transaction to rollback.
     *
     * @return The {@link Mono} that finishes this operation on the Service Bus resource.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be rolled back.
     *
     * @see ServiceBusReceiverAsyncClient#rollbackTransaction(ServiceBusTransactionContext)
     */
    public Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "rollbackTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        }
        if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return tracer.traceMono("ServiceBus.rollbackTransaction",
                connectionProcessor
                .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                .flatMap(transactionSession -> transactionSession.rollbackTransaction(new AmqpTransaction(
                    transactionContext.getTransactionId()))))
            .onErrorMap(this::mapError);
    }

    /**
     * Disposes of the {@link ServiceBusSenderAsyncClient}. If the client has a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        onClientClose.run();
    }

    private Mono<Void> sendIterable(Iterable<ServiceBusMessage> messages, ServiceBusTransactionContext transaction) {
        if (Objects.isNull(messages)) {
            return monoError(LOGGER, new NullPointerException("'messages' cannot be null."));
        }
        final Iterator<ServiceBusMessage> messagesItr = messages.iterator();
        if (messagesItr.hasNext()) {
            return sendNextIterableBatch(messagesItr.next(), messagesItr, transaction);
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> sendNextIterableBatch(ServiceBusMessage first, Iterator<ServiceBusMessage> messagesItr,
        ServiceBusTransactionContext transaction) {
        return this.createMessageBatch().flatMap(batch -> {
            ServiceBusMessage next = first;
            do {
                if (!batch.tryAddMessage(next)) {
                    if (next == first) {
                        return monoError(LOGGER,
                            new IllegalArgumentException("The message " + first + " is too big to send even in a batch."));
                    }
                    if (transaction != null) {
                        return this.sendMessages(batch, transaction).then(Mono.just(next));
                    } else {
                        return this.sendMessages(batch).then(Mono.just(next));
                    }
                }
                if (messagesItr.hasNext()) {
                    next = messagesItr.next();
                } else {
                    if (transaction != null) {
                        return this.sendMessages(batch, transaction).then(Mono.just(END));
                    } else {
                        return this.sendMessages(batch).then(Mono.just(END));
                    }
                }
            } while (true);
        }).flatMap(missed -> {
            if (missed == END) {
                return Mono.empty();
            } else {
                return sendNextIterableBatch(missed, messagesItr, transaction);
            }
        });
    }

    private Mono<Long> scheduleMessageInternal(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "scheduleMessage")));
        }
        if (Objects.isNull(message)) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        }

        if (Objects.isNull(scheduledEnqueueTime)) {
            return monoError(LOGGER, new NullPointerException("'scheduledEnqueueTime' cannot be null."));
        }

        return tracer.traceScheduleMono("ServiceBus.scheduleMessage",
                getSendLink().flatMap(link -> link.getLinkSize().flatMap(size -> {
                    int maxSize =  size > 0
                        ? size
                        : MAX_MESSAGE_LENGTH_BYTES;

                    return connectionProcessor
                        .flatMap(connection -> connection.getManagementNode(entityName, entityType))
                        .flatMap(managementNode -> managementNode.schedule(Arrays.asList(message), scheduledEnqueueTime,
                            maxSize, link.getLinkName(), transactionContext)
                        .next());
                })),
                message, message.getContext())
            .onErrorMap(this::mapError);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     * @param transactionContext to be set on batch message before sending to Service Bus.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     */
    private Mono<Void> sendInternal(ServiceBusMessageBatch batch, ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "sendMessages")));
        }
        if (Objects.isNull(batch)) {
            return monoError(LOGGER, new NullPointerException("'batch' cannot be null."));
        }

        if (batch.getMessages().isEmpty()) {
            LOGGER.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        LOGGER.atInfo()
            .addKeyValue("batchSize", batch.getCount())
            .log("Sending batch.");

        final List<org.apache.qpid.proton.message.Message> messages = Collections.synchronizedList(new ArrayList<>());


        batch.getMessages().forEach(serviceBusMessage -> {
            final org.apache.qpid.proton.message.Message message = messageSerializer.serialize(serviceBusMessage);
            final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                ? new MessageAnnotations(new HashMap<>())
                : message.getMessageAnnotations();

            message.setMessageAnnotations(messageAnnotations);
            messages.add(message);
        });

        final Mono<Void> sendMessage = getSendLink().flatMap(link -> {
            if (transactionContext != null && transactionContext.getTransactionId() != null) {
                final TransactionalState deliveryState = new TransactionalState();
                deliveryState.setTxnId(Binary.create(transactionContext.getTransactionId()));
                return messages.size() == 1
                    ? link.send(messages.get(0), deliveryState)
                    : link.send(messages, deliveryState);
            } else {
                return messages.size() == 1
                    ? link.send(messages.get(0))
                    : link.send(messages);
            }
        }).onErrorMap(RequestResponseChannelClosedException.class,
            e -> {
                // When the current connection is being disposed, the connectionProcessor can produce a new connection
                // if downstream request. In this context, treat RequestResponseChannelClosedException from
                // the RequestResponseChannel scoped to the current connection being disposed as retry-able so that retry
                // can obtain new connection.
                return new AmqpException(true, e.getMessage(), e, null);
            });

        final Mono<Void> sendWithRetry = withRetry(sendMessage, retryOptions,
            String.format("entityPath[%s], messages-count[%s]: Sending messages timed out.", entityName, batch.getCount()))
            .onErrorMap(this::mapError);

        return instrumentation.instrumentSendBatch("ServiceBus.send", sendWithRetry, batch.getMessages());
    }

    private Mono<Void> sendInternal(Flux<ServiceBusMessage> messages, ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_SENDER, "sendMessage")));
        }
        return withRetry(getSendLink(), retryOptions, "Failed to create send link " + linkName)
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateMessageBatchOptions batchOptions = new CreateMessageBatchOptions()
                        .setMaximumSizeInBytes(batchSize);
                    return messages.collect(new AmqpMessageCollector(isV2, batchOptions, 1,
                        link::getErrorContext, tracer, messageSerializer));
                })
                .flatMap(list -> sendInternalBatch(Flux.fromIterable(list), transactionContext)))
                .onErrorMap(this::mapError);
    }

    private Mono<Void> sendInternalBatch(Flux<ServiceBusMessageBatch> eventBatches,
        ServiceBusTransactionContext transactionContext) {
        return eventBatches
            .flatMap(messageBatch -> sendInternal(messageBatch, transactionContext))
            .then()
            .doOnError(error -> LOGGER.error("Error sending batch.", error));
    }

    private Mono<AmqpSendLink> getSendLink() {
        return connectionProcessor
            .flatMap(connection -> {
                if (!CoreUtils.isNullOrEmpty(viaEntityName)) {
                    return connection.createSendLink("VIA-".concat(viaEntityName), viaEntityName, retryOptions,
                        entityName, identifier);
                } else {
                    return connection.createSendLink(entityName, entityName, retryOptions, null, identifier);
                }
            })
            .doOnNext(next -> linkName.compareAndSet(null, next.getLinkName()));
    }

    private Throwable mapError(Throwable throwable) {
        if (!(throwable instanceof ServiceBusException)) {
            return new ServiceBusException(throwable, ServiceBusErrorSource.SEND);
        }
        return throwable;
    }

    private static class AmqpMessageCollector implements Collector<ServiceBusMessage, List<ServiceBusMessageBatch>,
        List<ServiceBusMessageBatch>> {
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final ServiceBusTracer tracer;
        private final MessageSerializer serializer;
        private final boolean isV2;

        private volatile ServiceBusMessageBatch currentBatch;

        AmqpMessageCollector(boolean isV2, CreateMessageBatchOptions options, Integer maxNumberOfBatches,
            ErrorContextProvider contextProvider, ServiceBusTracer tracer, MessageSerializer serializer) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.contextProvider = contextProvider;
            this.tracer = tracer;
            this.serializer = serializer;
            this.isV2 = isV2;
            currentBatch = new ServiceBusMessageBatch(isV2, maxMessageSize, contextProvider, tracer, serializer);
        }

        @Override
        public Supplier<List<ServiceBusMessageBatch>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<ServiceBusMessageBatch>, ServiceBusMessage> accumulator() {
            return (list, event) -> {
                ServiceBusMessageBatch batch = currentBatch;
                if (batch.tryAddMessage(event)) {
                    return;
                }

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    final String message = String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
                }

                currentBatch = new ServiceBusMessageBatch(isV2, maxMessageSize, contextProvider, tracer, serializer);
                currentBatch.tryAddMessage(event);
                list.add(batch);
            };
        }

        @Override
        public BinaryOperator<List<ServiceBusMessageBatch>> combiner() {
            return (existing, another) -> {
                existing.addAll(another);
                return existing;
            };
        }

        @Override
        public Function<List<ServiceBusMessageBatch>, List<ServiceBusMessageBatch>> finisher() {
            return list -> {
                ServiceBusMessageBatch batch = currentBatch;
                currentBatch = null;

                if (batch != null) {
                    list.add(batch);
                }
                return list;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
