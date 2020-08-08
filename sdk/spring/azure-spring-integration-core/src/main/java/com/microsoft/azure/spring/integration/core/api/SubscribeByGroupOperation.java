// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Operations for subscribing to a destination with a consumer group.
 *
 * @author Warren Zhu
 */
public interface SubscribeByGroupOperation extends Checkpointable {

    /**
     * Register a message consumer to a given destination with a given consumer group.
     * @param destination destination
     * @param consumerGroup consumer group name
     * @param consumer consumer
     * @param messagePayloadType message payload type
     * @return {@code true} if the consumer was subscribed or {@code false} if it
     * was already subscribed.
     */
    boolean subscribe(String destination, String consumerGroup, Consumer<Message<?>> consumer,
                      Class<?> messagePayloadType);

    default boolean subscribe(String destination, String consumerGroup, Consumer<Message<?>> consumer) {
        return this.subscribe(destination, consumerGroup, consumer, byte[].class);
    }

    /**
     * Un-register a message consumer with a given destination and consumer group.
     * @param destination destination
     * @param consumerGroup consumer group
     * @return {@code true} if the consumer was un-registered, or {@code false}
     * if was not registered.
     */
    boolean unsubscribe(String destination, String consumerGroup);
}
