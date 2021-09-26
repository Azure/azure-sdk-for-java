// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.PartitionSupplier;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;


/**
 * Operations for sending {@link Message} to a destination.
 *
 * @author Xiaolu Dai
 */
public interface SendOperation {

    /**
     * Send a {@link Message} to the given destination with a given partition supplier asynchronously.
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload class in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination asynchronously.
     * @param destination destination
     * @param message message
     * @param <T> payload class in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Message<T> message) {
        return sendAsync(destination, message, null);
    }

    /**
     * Send a {@link Message} to the given destination with a given partition supplier synchronously.
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload class in message
     * @return void
     */
    default <T> void send(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        sendAsync(destination, message, partitionSupplier).block();
    }

    /**
     * Send a {@link Message} to the given destination synchronously.
     * @param destination destination
     * @param message message
     * @param <T> payload class in message
     * @return void
     */
    default <T> void send(String destination, Message<T> message) {
        send(destination, message, null);
    }
}
