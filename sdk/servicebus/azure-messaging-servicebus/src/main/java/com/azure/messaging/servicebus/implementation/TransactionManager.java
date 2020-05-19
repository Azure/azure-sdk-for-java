package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public interface TransactionManager extends AutoCloseable{
    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @return {@link ServiceBusTransactionContext} which represent transaction in service Bus.
     */
    Mono<ByteBuffer> createTransaction();

    /**
     *
     * @param transactionContext to commit or rollback.
     * @param commit true to commit and false to rollback
     * @return {@link Mono<Void>}  which user can subscribe.
     */
    Mono<Void> completeTransaction(ServiceBusTransactionContext transactionContext, boolean commit);

}
