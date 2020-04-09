// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Updates the disposition status of a message given its lock token.
     *
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify);

    /**
     * This will return next available message to peek.
     *
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek();

    /**
     * @param fromSequenceNumber to peek message from.
     *
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
    Mono<Long> schedule(ServiceBusMessage message, Instant scheduledEnqueueTime,  int maxSendLinkSize);

    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber The sequence number of the scheduled message.
     * @return {@link Void} The successful completion represents the pending cancellation.
     */
    Mono<Void> cancelScheduledMessage(long sequenceNumber);

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, long fromSequenceNumber);

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     * @param maxMessages The number of messages.
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages);

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during the
     * Queue/Subscription creation (LockDuration). If processing of the message requires longer than this duration,
     * the lock needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param messageLock The {@link UUID} of the message {@link ServiceBusReceivedMessage} to be renewed.
     * @return {@link Instant} representing the pending renew.
     */
    Mono<Instant> renewMessageLock(UUID messageLock);

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage}. Deferred message can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber()}.
     * @return The received {@link ServiceBusReceivedMessage} message for given sequence number.
     */
    Mono<ServiceBusReceivedMessage> receiveDeferredMessage(ReceiveMode receiveMode, long sequenceNumber);

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumbers The sequence numbers from the {@link ServiceBusReceivedMessage#getSequenceNumber()}.
     * @return The received {@link ServiceBusReceivedMessage} message for given sequence number.
     */
    Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(ReceiveMode receiveMode, long... sequenceNumbers);

    @Override
    void close();
}
