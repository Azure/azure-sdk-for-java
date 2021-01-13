// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

/**
 * Azure Service Bus queues and topic subscriptions provide a secondary subqueue, called a dead-letter queue (DLQ).
 * The dead-letter queue doesn't need to be explicitly created and can't be deleted or otherwise managed
 * independent of the main entity.
 *
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead-letter Queues</a>
 */
public enum SubQueue {
    /**
     * Connect to the default entity (directly to the queue or subscription)
     */
    NONE,

    /**
     * This value to indicate dead-letter queue.
     * <p>
     * This is builder for creating  {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient} to
     * consume dead-letter messages from Service Bus entity.
     */
     DEAD_LETTER_QUEUE,

    /**
     * This value to indicate transfer dead-letter queue.
     *<p>
     * This is builder for creating  {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient} to
     * consume transfer dead-letter messages from Service Bus entity.
     */
    TRANSFER_DEAD_LETTER_QUEUE;
}
