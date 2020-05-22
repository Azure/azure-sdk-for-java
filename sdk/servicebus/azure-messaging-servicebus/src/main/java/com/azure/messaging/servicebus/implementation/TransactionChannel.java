package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Provides API to manage transaction on Service Bus namespace.
 */
public interface TransactionManager{
    /**
     * Creates the transaction in Service Bus namespace..
     *
     * @return {@link ServiceBusTransactionContext} which represent transaction in service Bus.
     */
    Mono<ByteBuffer> createTransaction();

    /**
     * Completes the given transaction.
     *
     * @param transactionContext to commit or rollback.
     * @param commit true to commit and false to rollback
     * @return {@link Mono<Void>}  which user can subscribe.
     */
    Mono<Void> completeTransaction(ServiceBusTransactionContext transactionContext, boolean commit);

}
