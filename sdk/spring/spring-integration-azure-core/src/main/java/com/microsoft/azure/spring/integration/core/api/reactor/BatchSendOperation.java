/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api.reactor;

import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.Collection;


/**
 * Operations for sending {@link Collection<Message>} to a destination.
 *
 * @author Xiaolu Dai
 */
public interface BatchSendOperation {

    /**
     * Send a {@link Collection<Message>} to the given destination with a given partition supplier.
     */
    <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                             PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection<Message>} to the given destination.
     */
    default <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }
}
