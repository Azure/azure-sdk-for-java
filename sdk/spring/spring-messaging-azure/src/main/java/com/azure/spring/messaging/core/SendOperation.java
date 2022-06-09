// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;


/**
 * Operations for sending {@link Message} to a destination.
 *
 */
public interface SendOperation {

    /**
     * Send a {@link Message} to the given destination asynchronously.
     * @param destination destination
     * @param message message
     * @param <T> payload class in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Message<T> message);

    /**
     * Send a {@link Message} to the given destination synchronously.
     * @param destination destination
     * @param message message
     * @param <T> payload class in message
     */
    default <T> void send(String destination, Message<T> message) {
        sendAsync(destination, message).block();
    }
}
