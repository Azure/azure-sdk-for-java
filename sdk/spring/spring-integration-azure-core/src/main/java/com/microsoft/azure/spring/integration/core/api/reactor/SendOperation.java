/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api.reactor;

import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
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
     */
    <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     */
    default <T> Mono<Void> sendAsync(String destination, Message<T> message) {
        return sendAsync(destination, message, null);
    }
}
