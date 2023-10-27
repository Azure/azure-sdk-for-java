// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * <p>A <b>synchronous</b> sender responsible for sending {@link ServiceBusMessage} to a queue or topic on
 * Azure Service Bus.</p>
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
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.instantiation -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderClient sender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * sender.sendMessage&#40;new ServiceBusMessage&#40;&quot;Foo bar&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.instantiation -->
 *
 * <p><strong>Sample: Send messages to a Service Bus resource</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderClient sender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * List&lt;ServiceBusMessage&gt; messages = Arrays.asList&#40;
 *     new ServiceBusMessage&#40;&quot;test-1&quot;&#41;,
 *     new ServiceBusMessage&#40;&quot;test-2&quot;&#41;&#41;;
 *
 * &#47;&#47; Creating a batch without options set.
 * ServiceBusMessageBatch batch = sender.createMessageBatch&#40;&#41;;
 * for &#40;ServiceBusMessage message : messages&#41; &#123;
 *     if &#40;batch.tryAddMessage&#40;message&#41;&#41; &#123;
 *         continue;
 *     &#125;
 *
 *     &#47;&#47; The batch is full. Send the current batch and create a new one.
 *     sender.sendMessages&#40;batch&#41;;
 *
 *     batch = sender.createMessageBatch&#40;&#41;;
 *
 *     &#47;&#47; Add the message we couldn't before.
 *     if &#40;!batch.tryAddMessage&#40;message&#41;&#41; &#123;
 *         throw new IllegalArgumentException&#40;&quot;Message is too large for an empty batch.&quot;&#41;;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; Send the final batch if there are any messages in it.
 * if &#40;batch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *     sender.sendMessages&#40;batch&#41;;
 * &#125;
 *
 * &#47;&#47; Continue using the sender and finally, dispose of the sender.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * sender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch -->
 *
 * <p><strong>Sample: Send messages using a size-limited {@link ServiceBusMessageBatch}</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions -->
 * <pre>
 * List&lt;ServiceBusMessage&gt; telemetryMessages = Arrays.asList&#40;firstMessage, secondMessage, thirdMessage&#41;;
 *
 * &#47;&#47; Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
 * &#47;&#47; In this case, all the batches created with these options are limited to 256 bytes.
 * CreateMessageBatchOptions options = new CreateMessageBatchOptions&#40;&#41;
 *     .setMaximumSizeInBytes&#40;256&#41;;
 *
 * ServiceBusMessageBatch currentBatch = sender.createMessageBatch&#40;options&#41;;
 *
 * &#47;&#47; For each telemetry message, we try to add it to the current batch.
 * &#47;&#47; When the batch is full, send it then create another batch to add more mesages to.
 * for &#40;ServiceBusMessage message : telemetryMessages&#41; &#123;
 *     if &#40;!currentBatch.tryAddMessage&#40;message&#41;&#41; &#123;
 *         sender.sendMessages&#40;currentBatch&#41;;
 *         currentBatch = sender.createMessageBatch&#40;options&#41;;
 *
 *         &#47;&#47; Add the message we couldn't before.
 *         if &#40;!currentBatch.tryAddMessage&#40;message&#41;&#41; &#123;
 *             throw new IllegalArgumentException&#40;&quot;Message is too large for an empty batch.&quot;&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; Send the final batch if there are any messages in it.
 * if &#40;currentBatch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *     sender.sendMessages&#40;currentBatch&#41;;
 * &#125;
 *
 * &#47;&#47; Continue using the sender and finally, dispose of the sender.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * sender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions -->
 *
 * <p><strong>Sample: Sending a message to a session-enabled queue</strong></p>
 *
 * <p>The snippet below demonstrates sending a message to a
 * <a href="https://learn.microsoft.com/azure/service-bus-messaging/message-sessions">Service Bus sessions</a>
 * enabled queue.  Setting {@link ServiceBusMessage#setMessageId(String)} property to "greetings" will send the message
 * to a Service Bus session with an id of "greetings".</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebussenderclient.sendMessage-session -->
 * <pre>
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusSenderClient sender = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .sender&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Setting sessionId publishes that message to a specific session, in this case, &quot;greeting&quot;.
 * ServiceBusMessage message = new ServiceBusMessage&#40;&quot;Hello world&quot;&#41;
 *     .setSessionId&#40;&quot;greetings&quot;&#41;;
 *
 * sender.sendMessage&#40;message&#41;;
 *
 * &#47;&#47; Dispose of the sender.
 * sender.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebussenderclient.sendMessage-session -->
 *
 * @see ServiceBusClientBuilder#sender()
 * @see ServiceBusSenderAsyncClient To communicate with a Service Bus resource using an asynchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusSenderClient implements AutoCloseable {
    private final ServiceBusSenderAsyncClient asyncClient;
    private final Duration tryTimeout;

    /**
     * Creates a new instance of {@link ServiceBusSenderClient} that sends messages to an Azure Service Bus.
     *
     * @throws NullPointerException if {@code asyncClient} or {@code tryTimeout} is null.
     */
    ServiceBusSenderClient(ServiceBusSenderAsyncClient asyncClient, Duration tryTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.tryTimeout = Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");
    }

    /**
     * Cancels the enqueuing of a scheduled message, if they are not already enqueued.
     *
     * @param sequenceNumber The sequence number of the message to cancel.
     *
     * @throws IllegalArgumentException if {@code sequenceNumber} is negative.
     * @throws ServiceBusException If the message could not be cancelled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void cancelScheduledMessage(long sequenceNumber) {
        asyncClient.cancelScheduledMessage(sequenceNumber).block(tryTimeout);
    }

    /**
     * Cancels the enqueuing of scheduled messages, if they are not already enqueued.
     *
     * @param sequenceNumbers The sequence numbers of messages to cancel.
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws ServiceBusException If the messages could not be cancelled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void cancelScheduledMessages(Iterable<Long> sequenceNumbers) {
        asyncClient.cancelScheduledMessages(sequenceNumbers).block(tryTimeout);
    }

    /**
     * Creates a {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @return A {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @throws ServiceBusException if the message batch could not be created.
     * @throws IllegalStateException if sender is already disposed.
     */
    public ServiceBusMessageBatch createMessageBatch() {
        return asyncClient.createMessageBatch().block(tryTimeout);
    }

    /**
     * Creates an {@link ServiceBusMessageBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link ServiceBusMessageBatch}.
     * @return A new {@link ServiceBusMessageBatch} configured with the given options.
     *
     * @throws NullPointerException if {@code options} is null.
     * @throws ServiceBusException if the message batch could not be created.
     * @throws IllegalStateException if sender is already disposed.
     * @throws IllegalArgumentException if {@link CreateMessageBatchOptions#getMaximumSizeInBytes()} is larger than
     *      maximum allowed size.
     */
    public ServiceBusMessageBatch createMessageBatch(CreateMessageBatchOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return asyncClient.createMessageBatch(options).block(tryTimeout);
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return asyncClient.getEntityPath();
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusSenderClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusSenderClient}.
     */
    public String getIdentifier() {
        return asyncClient.getIdentifier();
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     * @throws ServiceBusException if {@code message} is larger than the maximum allowed size of a single message or
     *      the message could not be sent.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void sendMessage(ServiceBusMessage message) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        asyncClient.sendMessage(message).block(tryTimeout);
    }

    /**
     * Sends a set of {@link ServiceBusMessage} to a Service Bus queue or topic using a batched approach.
     * If the size of messages exceed the maximum size of a single batch, an exception will be triggered and the send
     * will fail. By default, the message size is the max amount allowed on the link.
     *
     * @param messages Messages to be sent to Service Bus queue or topic.
     *
     * @throws NullPointerException if {@code messages} is {@code null}.
     * @throws ServiceBusException if the message could not be sent or {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void sendMessages(Iterable<ServiceBusMessage> messages) {
        asyncClient.sendMessages(messages).block(tryTimeout);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if the message batch could not be sent.
     */
    public void sendMessages(ServiceBusMessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        asyncClient.sendMessages(batch).block(tryTimeout);
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @throws NullPointerException if {@code message}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException if {@code message} is larger than the maximum allowed size of a single message or
     *      the message could not be sent.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void sendMessage(ServiceBusMessage message, ServiceBusTransactionContext transactionContext) {
        asyncClient.sendMessage(message, transactionContext).block(tryTimeout);
    }

    /**
     * Sends a set of {@link ServiceBusMessage} to a Service Bus queue or topic using a batched approach.
     * If the size of messages exceed the maximum size of a single batch, an exception will be triggered and the send
     * will fail. By default, the message size is the max amount allowed on the link.
     *
     * @param messages Messages to be sent to Service Bus queue or topic.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @throws NullPointerException if {@code messages}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException if the message could not be sent or {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void sendMessages(Iterable<ServiceBusMessage> messages, ServiceBusTransactionContext transactionContext) {
        asyncClient.sendMessages(messages, transactionContext).block(tryTimeout);
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @throws NullPointerException if {@code batch}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException if message batch could not be sent.
     * @throws IllegalStateException if sender is already disposed.
     */
    public void sendMessages(ServiceBusMessageBatch batch, ServiceBusTransactionContext transactionContext) {
        asyncClient.sendMessages(batch, transactionContext).block(tryTimeout);
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue or Topic.
     * @param scheduledEnqueueTime Datetime at which the message should appear in the Service Bus queue or topic.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the message could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Long scheduleMessage(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime) {
        return asyncClient.scheduleMessage(message, scheduledEnqueueTime).block(tryTimeout);
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue or Topic.
     * @param scheduledEnqueueTime Datetime at which the message should appear in the Service Bus queue or topic.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException if {@code message}, {@code scheduledEnqueueTime}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException If the message could not be scheduled.
     */
    public Long scheduleMessage(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        return asyncClient.scheduleMessage(message, scheduledEnqueueTime, transactionContext).block(tryTimeout);
    }

    /**
     * Sends a batch of scheduled messages to the Azure Service Bus entity this sender is connected to. A scheduled
     * message is enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param messages Messages to be sent to the Service Bus queue or topic.
     * @param scheduledEnqueueTime Instant at which the message should appear in the Service Bus queue or topic.
     *
     * @return Sequence numbers of the scheduled messages which can be used to cancel the messages.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException If {@code messages} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the messages could not be scheduled.
     */
    public Iterable<Long> scheduleMessages(Iterable<ServiceBusMessage> messages, OffsetDateTime scheduledEnqueueTime) {
        return new IterableStream<>(asyncClient.scheduleMessages(messages, scheduledEnqueueTime));
    }

    /**
     * Sends a batch of scheduled messages to the Azure Service Bus entity this sender is connected to. A scheduled
     * message is enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param messages Messages to be sent to the Service Bus Queue or Topic.
     * @param scheduledEnqueueTime Instant at which the message should appear in the Service Bus queue or topic.
     * @param transactionContext Transaction to associate with the operation.
     *
     * @return Sequence numbers of the scheduled messages which can be used to cancel the messages.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException If {@code messages}, {@code scheduledEnqueueTime}, {@code transactionContext} or
     *      {@code transactionContext.transactionId} is {@code null}.
     * @throws ServiceBusException If the messages could not be scheduled or the {@code message} is larger than
     *      the maximum size of the {@link ServiceBusMessageBatch}.
     */
    public Iterable<Long> scheduleMessages(Iterable<ServiceBusMessage> messages, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        return new IterableStream<>(asyncClient.scheduleMessages(messages, scheduledEnqueueTime, transactionContext));
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along to all
     * operations that need to be in this transaction.
     *
     * @return A new {@link ServiceBusTransactionContext}.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if a transaction cannot be created.
     *
     * @see ServiceBusReceiverClient#createTransaction()
     */
    public ServiceBusTransactionContext createTransaction() {
        return asyncClient.createTransaction().block(tryTimeout);
    }

    /**
     * Commits the transaction given {@link ServiceBusTransactionContext}.
     *
     * @param transactionContext to be committed.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be committed.
     *
     * @see ServiceBusReceiverClient#commitTransaction(ServiceBusTransactionContext)
     */
    public void commitTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.commitTransaction(transactionContext).block(tryTimeout);
    }

    /**
     * Rollbacks the transaction given and all operations associated with it.
     *
     * @param transactionContext The transaction to rollback.
     *
     * @throws IllegalStateException if sender is already disposed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws ServiceBusException if the transaction could not be rolled back.
     *
     * @see ServiceBusReceiverClient#rollbackTransaction(ServiceBusTransactionContext)
     */
    public void rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.rollbackTransaction(transactionContext).block(tryTimeout);
    }

    /**
     * Disposes of the {@link ServiceBusSenderClient}. If the client has a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
