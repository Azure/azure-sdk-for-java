// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
    String getTopicName();
}
