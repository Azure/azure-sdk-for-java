// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api.reactor;

import com.azure.spring.integration.core.api.PartitionSupplier;
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
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return Mono Void
     */
    <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                             PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     * @return Mono Void
     */
    default <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }
}
