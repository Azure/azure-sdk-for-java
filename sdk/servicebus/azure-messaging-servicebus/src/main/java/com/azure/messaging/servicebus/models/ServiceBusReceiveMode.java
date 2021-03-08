// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

/**
 * Defines the modes for receiving messages.
 *
 * @see ServiceBusReceiverClientBuilder#receiveMode(ServiceBusReceiveMode)
 * @see ServiceBusSessionReceiverClientBuilder#receiveMode(ServiceBusReceiveMode)
 */
public enum ServiceBusReceiveMode {
    /**
     * In this mode, received message is not deleted from the queue or subscription, instead it is temporarily locked to
     * the receiver, making it invisible to other receivers. Then the service waits for one of the following events:
     * <ul>
     * <li>If the receiver processes the message successfully, it calls
     * {@link ServiceBusReceiverClient#complete(ServiceBusReceivedMessage)} or
     * {@link ServiceBusReceiverAsyncClient#complete(ServiceBusReceivedMessage)} and the message will be deleted.</li>
     * <li>If the receiver decides that it can't process the message successfully, it calls
     * {@link ServiceBusReceiverClient#abandon(ServiceBusReceivedMessage)} or
     * {@link ServiceBusReceiverAsyncClient#abandon(ServiceBusReceivedMessage)} and the message will be unlocked and
     * made available to other receivers.</li>
     * <li>If the receiver wants to defer the processing of the message to a later point in time, it calls
     * {@link ServiceBusReceiverClient#defer(ServiceBusReceivedMessage)} or
     * {@link ServiceBusReceiverAsyncClient#defer(ServiceBusReceivedMessage)} and the message will be deferred. A
     * deferred can only be received by its sequence number.</li>
     * <li>If the receiver wants to dead-letter the message, it calls
     * {@link ServiceBusReceiverClient#deadLetter(ServiceBusReceivedMessage)} or
     * {@link ServiceBusReceiverAsyncClient#deadLetter(ServiceBusReceivedMessage)} and the message will
     * be moved to a special sub-queue called dead-letter queue.</li>
     * <li>If the receiver calls neither of these methods within a configurable period of time
     * (by default, 60 seconds), the service assumes the receiver has failed. In this case, it behaves as if the
     * receiver had called <code>abandon</code>, making the message available to other receivers</li>
     * </ul>
     */
    PEEK_LOCK,

    /**
     * In this mode, received message is removed from the queue or subscription and immediately deleted. This option is
     * simple, but if the receiver crashes before it finishes processing the message, the message is lost. Because it
     * has been removed from the queue, no other receiver can access it.
     */
    RECEIVE_AND_DELETE
}
