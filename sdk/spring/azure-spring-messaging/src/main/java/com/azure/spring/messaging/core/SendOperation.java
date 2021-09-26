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
     * Send a {@link Message} to the given destination with a given partition supplier.
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload class in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     * @param destination destination
     * @param message message
     * @param <T> payload class in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Message<T> message) {
        return sendAsync(destination, message, null);
    }
}
