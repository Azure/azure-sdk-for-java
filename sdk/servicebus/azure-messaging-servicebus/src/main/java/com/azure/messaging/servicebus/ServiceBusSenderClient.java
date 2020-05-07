// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ServiceClient;
import com.azure.messaging.servicebus.models.CreateBatchOptions;

import java.time.Duration;
import java.time.Instant;
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
    public void send(ServiceBusMessage message) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        asyncClient.send(message).block(tryTimeout);
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
    public void send(Iterable<ServiceBusMessage> messages) {
        asyncClient.send(messages).block(tryTimeout);
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue or Topic.
     * @param scheduledEnqueueTime Instant at which the message should appear in the Service Bus queue or topic.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message} or {@code scheduledEnqueueTime} is {@code null}.
     */
    public Long scheduleMessage(ServiceBusMessage message, Instant scheduledEnqueueTime) {
        return asyncClient.scheduleMessage(message, scheduledEnqueueTime).block(tryTimeout);
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
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     */
    public void send(ServiceBusMessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        asyncClient.send(batch).block(tryTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
