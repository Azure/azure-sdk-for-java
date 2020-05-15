package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionManager extends AutoCloseable{
    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     *
     * @return {@link Transaction} which represent transaction in service Bus.
     */
    Mono<Transaction> createTransaction();

}
