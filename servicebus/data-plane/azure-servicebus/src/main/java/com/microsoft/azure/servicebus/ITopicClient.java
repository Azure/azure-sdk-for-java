// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

/**
 * TopicClient can be used for all basic interactions with a Service Bus topic.
 * {@code
 *
 * }
 */
public interface ITopicClient extends IMessageSender, IMessageBrowser, IMessageEntityClient {

    /**
     * Get the name of the topic
     * @return the name of the topic
     */
    public String getTopicName();
}
