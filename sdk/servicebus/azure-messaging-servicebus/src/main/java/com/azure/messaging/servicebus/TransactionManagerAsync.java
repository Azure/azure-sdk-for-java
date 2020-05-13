// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Provides API to manage transactions.
 */
public class TransactionManagerAsync {

    private String namespaceUri;
    private Consumer<Transaction> onCompleteNotify;

    TransactionManagerAsync(String namespaceUri, Duration timeout) {
        this.namespaceUri = namespaceUri;
    }

    /**
     * Starts a new service side transaction. The {@link Transaction} should be passed to all operations that
     * needs to be in this transaction.
     * @return a new transaction
     */
    public Mono<Transaction> startTransaction() {
        new Transaction(null);
        return null;
    }

    /**
     * Ends a transaction
     * @param transaction The transaction object.
     * @param commit A boolean value of <code>true</code> indicates transaction to be committed. A value of
     * <code>false</code> indicates a transaction rollback.
     * @return A void
     */
    public Mono<Void> endTransaction(Transaction transaction, boolean commit) {
        // call onCompleteNotify
        transaction.notifyCompletion(commit);
        return null;
    }
}
