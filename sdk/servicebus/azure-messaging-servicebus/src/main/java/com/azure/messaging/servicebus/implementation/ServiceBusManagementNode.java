// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * The management node for performing Service Bus metadata operations, scheduling, and inspecting messages.
 */
public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber The sequence number of the scheduled message.
     *
     * @return {@link Void} The successful completion represents the pending cancellation.
     */
    Mono<Void> cancelScheduledMessage(long sequenceNumber, String associatedLinkName);

    /**
     * Gets the session state.
     *
     * @param sessionId Id of the session.
     * @return The state of the session.
     */
    Mono<byte[]> getSessionState(String sessionId, String associatedLinkName);

    /**
     * @param fromSequenceNumber to peek message from.
     *
     * @return {@link Mono} of {@link ServiceBusReceivedMessage}.
     */
    Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber, String sessionId, String associatedLinkName);

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param fromSequenceNumber The sequence number from where to read the message.
     *
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    Flux<ServiceBusReceivedMessage> peek(long fromSequenceNumber, String sessionId, String associatedLinkName,
        int maxMessages);

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage}. Deferred messages can only be received by using sequence
     * number.
     *
     * @param receiveMode Mode to receive messages.
     * @param sequenceNumbers The sequence numbers from the {@link ServiceBusReceivedMessage#getSequenceNumber()}.
     * @param sessionId Identifier for the session.
     *
     * @return The received {@link ServiceBusReceivedMessage} message for given sequence number.
     */
    Flux<ServiceBusReceivedMessage> receiveDeferredMessages(ReceiveMode receiveMode, String sessionId,
        String associatedLinkName, Iterable<Long> sequenceNumbers);

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during the
     * Queue/Subscription creation (LockDuration). If processing of the message requires longer than this duration,
     * the lock needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param messageLock The lock token of the message {@link ServiceBusReceivedMessage} to be renewed.
     * @return {@link Instant} representing the pending renew.
     */
    Mono<Instant> renewMessageLock(String messageLock, String associatedLinkName);

    /**
     * Renews the lock on the session.
     *
     * @param sessionId Identifier for the session.
     * @return The next expiration time for the session.
     */
    Mono<Instant> renewSessionLock(String sessionId, String associatedLinkName);

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time. This is an asynchronous method
     * returning a CompletableFuture which completes when the message is sent to the entity. The CompletableFuture, on
     * completion, returns the sequence number of the scheduled message which can be used to cancel the scheduling of
     * the message.
     *
     * @param message The message to be sent to the entity.
     * @param scheduledEnqueueTime The {@link Instant} at which the message should be enqueued in the entity.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @return The sequence number representing the pending send, which returns the sequence number of the scheduled
     *     message. This sequence number can be used to cancel the scheduling of the message.
     */
    Mono<Long> schedule(ServiceBusMessage message, Instant scheduledEnqueueTime, int maxSendLinkSize,
        String associatedLinkName, ServiceBusTransactionContext transactionContext);

    /**
     * Updates the session state.
     *
     * @param sessionId Identifier for the session.
     * @param state State to update session.
     *
     * @return A Mono that completes when the state is updated.
     */
    Mono<Void> setSessionState(String sessionId, byte[] state, String associatedLinkName);

    /**
     * Updates the disposition status of a message given its lock token.
     *
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify, String sessionId,
        String associatedLinkName, ServiceBusTransactionContext transactionContext);

    @Override
    void close();
}
