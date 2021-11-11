// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;


/**
 * Operations for sending {@link Collection}&lt;{@link Message}&gt; to a destination.
 *
 * @author Warren Zhu
 *
 * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link com.azure.spring.integration.core.api.reactor.BatchSendOperation}. From version 4.0.0, the reactor API support
 * will be moved to com.azure.spring.messaging.core.BatchSendOperation.
 */
@Deprecated
public interface BatchSendOperation {

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier.
     * @param destination destination
     * @param messages message
     * @param partitionSupplier partition supplier
     * @param <T> payload class type in message
     * @return Future instance
     *
     * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.BatchSendOperation}. From version 4.0.0, the reactor API support
     * will be moved to com.azure.spring.messaging.core.BatchSendOperation.
     */
    @Deprecated
    <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                          PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination.
     * @param destination destination
     * @param messages messages
     * @param <T> payload class type in message
     * @return Future instance
     *
     * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.BatchSendOperation}. From version 4.0.0, the reactor API support
     * will be moved to com.azure.spring.messaging.core.BatchSendOperation.
     */
    @Deprecated
    default <T> CompletableFuture<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }
}
