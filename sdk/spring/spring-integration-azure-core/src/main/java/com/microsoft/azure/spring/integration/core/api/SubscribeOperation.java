/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Operations for subscribing to a destination.
 *
 * @author Warren Zhu
 */
public interface SubscribeOperation extends Checkpointable {

    /**
     * Register a message consumer to a given destination.
     *
     * @return {@code true} if the consumer was subscribed or {@code false} if it
     * was already subscribed.
     */
    boolean subscribe(String destination, Consumer<Message<?>> consumer, Class<?> messagePayloadType);

    default boolean subscribe(String destination, Consumer<Message<?>> consumer) {
        return this.subscribe(destination, consumer, byte[].class);
    }

    /**
     * Un-register a message consumer.
     *
     * @return {@code true} if the consumer was un-registered, or {@code false}
     * if was not registered.
     */
    boolean unsubscribe(String destination);
}
