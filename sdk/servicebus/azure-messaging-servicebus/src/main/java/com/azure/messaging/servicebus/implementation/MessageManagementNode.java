// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.MessageLockToken;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation interface that scopes operations for message management.
 */
public interface MessageManagementNode {
    /**
     * Abandon a {@link ServiceBusReceivedMessage message} with its lock token. This will make the message available
     * again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    Mono<Void> abandon(MessageLockToken lockToken);

    /**
     * Completes a {@link ServiceBusReceivedMessage message} using its lock token. This will delete the message from the
     * service.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     */
    Mono<Void> complete(MessageLockToken lockToken);

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the deadletter sub-queue.
     *
     * @param lockToken Lock token of the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns a null lock token.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    Mono<Void> deadLetter(MessageLockToken lockToken);

    /**
     * Asynchronously renews the lock on the specified message. The lock will be renewed based on the setting specified
     * on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the Queue creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the
     * lock is reset to the entity's LockDuration value.
     *
     * @param lockToken Lock token of the message to renew.
     *
     * @return The new expiration time for the message.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws UnsupportedOperationException if the receiver was opened in {@link ReceiveMode#RECEIVE_AND_DELETE}
     *     mode.
     * @throws IllegalArgumentException if {@link MessageLockToken#getLockToken()} returns an empty value.
     */
    Mono<Instant> renewMessageLock(MessageLockToken lockToken);
}
