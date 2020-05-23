package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Provides API to manage transaction on Service Bus namespace.
 */
public interface TransactionChannel {
    /**
     * Creates the transaction in Service Bus namespace..
     *
     * @return {@link ByteBuffer} which represent transaction in service Bus.
     */
    Mono<ByteBuffer> transactionSelect();

    /**
     * Completes the given transaction.
     *
     * @param transactionContext to commit.
     * @return {@link Mono<Void>} which user can subscribe.
     */
    Mono<Void> transactionCommit(ServiceBusTransactionContext transactionContext);

    /**
     * Completes the given transaction.
     *
     * @param transactionContext to rollback.
     * @return {@link Mono<Void>} which user can subscribe.
     */
    Mono<Void> transactionRollback(ServiceBusTransactionContext transactionContext);

}
