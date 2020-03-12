// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

import reactor.core.publisher.Mono;

import java.time.Instant;


/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {

    /**
     * This will return next available message to peek.
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek();

    /**
     * @param fromSequenceNumber to peek message from.
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber);

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time. This is an asynchronous method
     * returning a CompletableFuture which completes when the message is sent to the entity. The CompletableFuture,
     * on completion, returns the sequence number of the scheduled message which can be used to cancel the scheduling
     * of the message.
     *
     * @param message The message to be sent to the entity.
     * @param scheduledEnqueueTime The {@link Instant} at which the message should be enqueued in the entity.
     * @return The sequence number representing the pending send, which returns the sequence number of
      * the scheduled message. This sequence number can be used to cancel the scheduling of the message.
     */
    Mono<Long> schedule(ServiceBusMessage message, Instant scheduledEnqueueTime);

    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber The sequence number of the scheduled message.
     * @return {@link Void} The successful completion represents the pending cancellation.
     */
    Mono<Void> cancelSchedule(long sequenceNumber);

    @Override
    void close();
}
