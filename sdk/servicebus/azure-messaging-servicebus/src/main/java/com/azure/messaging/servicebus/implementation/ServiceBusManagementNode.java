// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


/**
 * The management node for fetching metadata about the Service Bus and peek operation.
 */
public interface ServiceBusManagementNode extends AutoCloseable {
    /**
     * Completes a message given its lock token.
     * @param lockToken Lock token to complete
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    Mono<Void> complete(UUID lockToken);

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
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during the
     * Queue/Subscription creation (LockDuration). If processing of the message requires longer than this duration,
     * the lock needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param messageForLockRenew The {@link ServiceBusReceivedMessage} to be renewed
     * @return {@link Instant} representing the pending renew.
     */
    Mono<Instant> renewMessageLock(ServiceBusReceivedMessage messageForLockRenew);

    @Override
    void close();
}
