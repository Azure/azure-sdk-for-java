// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

/**
 * Defines values for sub queue type.
 */
public enum SubQueue {
    /** Connect to the default entity (directly to the queue or subscription) **/
    NONE,

    /**
     * Azure Service Bus queues and topic subscriptions provide a secondary subqueue, called a dead-letter queue (DLQ).
     * The dead-letter queue doesn't need to be explicitly created and can't be deleted or otherwise managed
     * independent of the main entity.
     * <p>
     * This is builder for creating  {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient} to
     * consume dead-letter messages from Service Bus entity.
     */

     DEAD_LETTER_QUEUE,

    /**
     * This value indicate transfer deadletter queue.
     */
    TRANSFER_QUEUE;
}
