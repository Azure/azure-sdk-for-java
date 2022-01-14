// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.checkpoint.Checkpointable;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Operations for subscribing to a destination.
 *
 */
public interface SubscribeOperation extends Checkpointable {

    /**
     * Register a message consumer to a given destination.
     * @param destination destination
     * @param consumer consumer
     * @param messagePayloadType message payload type
     * @return {@code true} if the consumer was subscribed or {@code false} if it
     * was already subscribed.
     */
    boolean subscribe(String destination, Consumer<Message<?>> consumer, Class<?> messagePayloadType);

    /**
     * Register a message consumer to a given destination using the byte array class of payload by default.
     * @param destination destination
     * @param consumer consumer
     * @return {@code true} if the consumer was subscribed or {@code false} if it
     * was already subscribed.
     */
    default boolean subscribe(String destination, Consumer<Message<?>> consumer) {
        return this.subscribe(destination, consumer, byte[].class);
    }

    /**
     * Un-register a message consumer.
     * @param destination destination
     * @return {@code true} if the consumer was un-registered, or {@code false}
     * if was not registered.
     */
    boolean unsubscribe(String destination);
}
