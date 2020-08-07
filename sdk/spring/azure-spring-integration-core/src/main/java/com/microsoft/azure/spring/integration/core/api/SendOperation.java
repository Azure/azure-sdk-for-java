// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;


/**
 * Operations for sending {@link Message} to a destination.
 *
 * @author Warren Zhu
 */
public interface SendOperation {

    /**
     * Send a {@link Message} to the given destination with a given partition supplier.
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return future instance
     */
    <T> CompletableFuture<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     * @param destination destination
     * @param message message
     * @param <T> payload type in message
     * @return future instance
     */
    default <T> CompletableFuture<Void> sendAsync(String destination, Message<T> message) {
        return sendAsync(destination, message, null);
    }
}
