// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;


/**
 * Operations for sending {@link Collection}&lt;{@link Message}&gt; to a destination.
 *
 * @author Warren Zhu
 */
public interface BatchSendOperation {

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier.
     * @param destination destination
     * @param messages message
     * @param partitionSupplier partition supplier
     * @param <T> payload class type in message
     * @return Future instance
     */
    <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                          PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination.
     * @param destination destination
     * @param messages messages
     * @param <T> payload class type in message
     * @return Future instance
     */
    default <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }
}
