// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

/**
 * QueueClient can be used for all basic interactions with a Service Bus Queue.
 */
public interface IQueueClient extends IMessageSender, IMessageAndSessionPump, IMessageEntityClient {

    /**
     * Gets the {@link ReceiveMode} of the current receiver
     *
     * @return The receive mode.
     */
    ReceiveMode getReceiveMode();

    /**
     * Gets the name of the queue.
     *
     * @return The name of the queue.
     */
    String getQueueName();
}
