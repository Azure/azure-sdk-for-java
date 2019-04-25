// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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
    public ReceiveMode getReceiveMode();

    /**
     * Gets the name of the queue.
     *
     * @return The name of the queue.
     */
    public String getQueueName();
}
