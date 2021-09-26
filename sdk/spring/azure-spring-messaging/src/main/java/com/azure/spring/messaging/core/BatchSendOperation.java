// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.PartitionSupplier;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.Collection;


/**
 * Operations for sending {@link Collection}&lt;{@link Message}&gt; to a destination.
 *
 * @author Xiaolu Dai
 */
public interface BatchSendOperation {

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier asynchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                             PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination asynchronously.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier synchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> void send(String destination, Collection<Message<T>> messages, PartitionSupplier partitionSupplier) {
        sendAsync(destination, messages, partitionSupplier).block();
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination synchronously.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> void send(String destination, Collection<Message<T>> messages) {
        send(destination, messages, null);
    }
}
