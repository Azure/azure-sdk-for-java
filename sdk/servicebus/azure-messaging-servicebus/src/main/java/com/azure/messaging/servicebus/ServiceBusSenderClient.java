// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ServiceClient;
import com.azure.messaging.servicebus.models.CreateBatchOptions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A <b>synchronous</b> sender responsible for sending {@link ServiceBusMessage} to  specific queue or topic on
 * Azure Service Bus.
 *
 * <p><strong>Create an instance of sender</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebussenderclient.instantiation}
 *
 * <p><strong>Send messages to a Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebussenderclient.createBatch}
 *
 * <p><strong>Send messages using a size-limited {@link ServiceBusMessageBatch}</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebussenderclient.createBatch#CreateBatchOptions-int}
 *
 * @see ServiceBusClientBuilder#sender()
 * @see ServiceBusSenderAsyncClient To communicate with a Service Bus resource using an asynchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public class ServiceBusSenderClient implements AutoCloseable {
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
     * Cancels the enqueuing of an already scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber of the scheduled message to cancel.
     *
     * @throws IllegalArgumentException if {@code sequenceNumber} is negative.
     */
    public void cancelScheduledMessage(long sequenceNumber) {
        asyncClient.cancelScheduledMessage(sequenceNumber).block(tryTimeout);
    }

    /**
     * Creates a {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @return A {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     */
    public ServiceBusMessageBatch createBatch() {
        return asyncClient.createBatch().block(tryTimeout);
    }

    /**
     * Creates an {@link ServiceBusMessageBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link ServiceBusMessageBatch}.
     * @return A new {@link ServiceBusMessageBatch} configured with the given options.
     * @throws NullPointerException if {@code options} is null.
     */
    public ServiceBusMessageBatch createBatch(CreateBatchOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return asyncClient.createBatch(options).block(tryTimeout);
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
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
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
     * @throws AmqpException if {@code messages} is larger than the maximum allowed size of a single batch.
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
     * {@code transactionContext.transactionId} is {@code null}.
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
     * {@code transactionContext.transactionId} is {@code null}.
     * @throws AmqpException if {@code messages} is larger than the maximum allowed size of a single batch.
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
     * {@code transactionContext.transactionId} is {@code null}.
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
     * @throws NullPointerException if {@code message}, {@code scheduledEnqueueTime}, {@code transactionContext} or
     * {@code transactionContext.transactionId} is {@code null}.
     * @throws NullPointerException if  is null.
     */
    public Long scheduleMessage(ServiceBusMessage message, OffsetDateTime scheduledEnqueueTime,
        ServiceBusTransactionContext transactionContext) {
        return asyncClient.scheduleMessage(message, scheduledEnqueueTime, transactionContext).block(tryTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        asyncClient.close();
    }

    /**
     * Starts a new transaction on Service Bus. The {@link ServiceBusTransactionContext} should be passed along with
     * {@link ServiceBusReceivedMessage} or {@code lockToken} to all operations that needs to be in this transaction.
     *
     * @return a new {@link ServiceBusTransactionContext}.
     */
    public ServiceBusTransactionContext createTransaction() {
        return asyncClient.createTransaction().block(tryTimeout);
    }

    /**
     * Commits the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext to be committed.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     */
    public void commitTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.commitTransaction(transactionContext).block(tryTimeout);
    }

    /**
     * Rollbacks the transaction given {@link ServiceBusTransactionContext}. This will make a call to Service Bus.
     *
     * @param transactionContext to be rollbacked.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     */
    public void rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        asyncClient.rollbackTransaction(transactionContext).block(tryTimeout);
    }
}
