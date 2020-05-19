package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.Transaction;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public interface TransactionManager extends AutoCloseable{
    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @return {@link Transaction} which represent transaction in service Bus.
     */
    Mono<ByteBuffer> createTransaction();

    /**
     *
     * @param transaction to commit or rollback.
     * @param commit true to commit and false to rollback
     * @return {@link Mono<Void>}  which user can subscribe.
     */
    Mono<Void> completeTransaction(Transaction transaction, boolean commit);

}
