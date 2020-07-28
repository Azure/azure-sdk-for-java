/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;


/**
 * Operations for sending {@link Collection<Message>} to a destination.
 *
 * @author Warren Zhu
 */
public interface BatchSendOperation {

    /**
     * Send a {@link Collection<Message>} to the given destination with a given partition supplier.
     */
    <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages,
            PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection<Message>} to the given destination.
     */
    default <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }
}
