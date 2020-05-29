// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Provides API to manage transaction on Service Bus namespace.
 */
public interface TransactionManager {
    /**
     * Creates the transaction in Service Bus namespace..
     *
     * @return {@link ByteBuffer} which represent transaction in service Bus.
     */
    Mono<ByteBuffer> createTransaction();

    /**
     * Commits the given transaction.
     *
     * @param transactionContext to commit.
     * @return {@link Mono} which user can subscribe.
     */
    Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext);

    /**
     * Rollbacks the given transaction.
     *
     * @param transactionContext to rollback.
     * @return {@link Mono} which user can subscribe.
     */
    Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext);

}
