// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The management node for performing Service Bus metadata operations, scheduling, and inspecting messages.
 */
public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Cancels the enqueuing of an already sent scheduled messages, if it was not already enqueued.
     *
     * @param sequenceNumbers The sequence number of the scheduled messages.
     *
     * @return {@link Void} The successful completion represents the pending cancellation.
     */
    Mono<Void> cancelScheduledMessages(Iterable<Long> sequenceNumbers, String associatedLinkName);

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
    Flux<ServiceBusReceivedMessage> receiveDeferredMessages(ServiceBusReceiveMode receiveMode, String sessionId,
        String associatedLinkName, Iterable<Long> sequenceNumbers);

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ServiceBusReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during the
     * Queue/Subscription creation (LockDuration). If processing of the message requires longer than this duration,
     * the lock needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param messageLock The lock token of the message {@link ServiceBusReceivedMessage} to be renewed.
     * @return {@link OffsetDateTime} representing the pending renew.
     */
    Mono<OffsetDateTime> renewMessageLock(String messageLock, String associatedLinkName);

    /**
     * Renews the lock on the session.
     *
     * @param sessionId Identifier for the session.
     * @return The next expiration time for the session.
     */
    Mono<OffsetDateTime> renewSessionLock(String sessionId, String associatedLinkName);

    /**
     * Sends a scheduled {@link List} of messages to the Azure Service Bus entity this sender is connected to.
     * Scheduled messages are enqueued and made available to receivers only at the scheduled enqueue time.
     * This is an asynchronous method returning a CompletableFuture which completes when the message is sent to the
     * entity. The CompletableFuture, on completion, returns the sequence numbers of the scheduled messages which can be
     * used to cancel the scheduling of the message.
     *
     * @param messages The messages to be sent to the entity.
     * @param scheduledEnqueueTime The {@link OffsetDateTime} at which the message should be enqueued in the entity.
     * @param transactionContext to be set on message before sending to Service Bus.
     *
     * @return The sequence numbers representing the pending send, which returns the sequence numbers of the scheduled
     *     messages. These sequence numbers can be used to cancel the scheduling of the messages.
     */
    Flux<Long> schedule(List<ServiceBusMessage> messages, OffsetDateTime scheduledEnqueueTime, int maxSendLinkSize,
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
