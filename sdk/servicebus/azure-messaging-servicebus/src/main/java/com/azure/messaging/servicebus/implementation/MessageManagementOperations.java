// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Interface for settling and renewing messages.
 */
public interface MessageManagementOperations {
    /**
     * Updates the disposition status of a message given its lock token.
     *
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState);

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on
     * the setting specified on the entity. When a message is received in {@link ServiceBusReceiveMode#PEEK_LOCK} mode,
     * the message is locked on the server for this receiver instance for a duration as specified during the
     * Queue/Subscription creation (LockDuration). If processing of the message requires longer than this duration,
     * the lock needs to be renewed. For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param lockToken The {@link UUID} of the message {@link ServiceBusReceivedMessage} to be renewed.
     * @return {@link OffsetDateTime} representing the pending renew.
     */
    Mono<OffsetDateTime> renewMessageLock(String lockToken, String associatedLinkName);
}
