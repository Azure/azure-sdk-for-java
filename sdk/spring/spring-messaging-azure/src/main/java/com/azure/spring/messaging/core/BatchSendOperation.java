// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.PartitionSupplier;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
     * @param maxSizeInBytes the maximum size to allow for the batch sending
     * @param maxWaitTime the maximum wait time for buffering a batch of messages
     * @param <T> payload type in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                             PartitionSupplier partitionSupplier, int maxSizeInBytes, Duration maxWaitTime);

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier asynchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param maxSizeInBytes the maximum size to allow for the batch sending
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                             PartitionSupplier partitionSupplier, int maxSizeInBytes) {
        return sendAsync(destination, messages, partitionSupplier, 0, Duration.ofMillis(0));
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier asynchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                     PartitionSupplier partitionSupplier) {
        return sendAsync(destination, messages, partitionSupplier, 0);
    }

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
     * @param maxSizeInBytes the maximum size to allow for the batch sending
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> void send(String destination, Collection<Message<T>> messages, PartitionSupplier partitionSupplier,
                          int maxSizeInBytes, Duration maxWaitTime) {
        sendAsync(destination, messages, partitionSupplier, maxSizeInBytes, maxWaitTime).block();
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier synchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param maxSizeInBytes the maximum size to allow for the batch sending
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> void send(String destination, Collection<Message<T>> messages, PartitionSupplier partitionSupplier,
                          int maxSizeInBytes) {
        send(destination, messages, partitionSupplier, maxSizeInBytes, Duration.ofMillis(0));
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier synchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     */
    default <T> void send(String destination, Collection<Message<T>> messages, PartitionSupplier partitionSupplier) {
        send(destination, messages, partitionSupplier, 0);
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination synchronously.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     */
    default <T> void send(String destination, Collection<Message<T>> messages) {
        send(destination, messages, null);
    }
}
