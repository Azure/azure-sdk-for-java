// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;

/**
 * Provides API to manage transactions.
 */
public class TransactionManager {
    private TransactionManagerAsync asynClient;
    private Duration timeout;

    TransactionManager(TransactionManagerAsync asynClient, Duration timeout) {
        this.asynClient = asynClient;
        this.timeout = timeout;
    }

    /**
     * Starts a new service side transaction. The {@link Transaction} should be passed to all operations that
     * needs to be in this transaction.
     * @return a new transaction
     */
    public Transaction beginTransaction() {
        return asynClient.beginTransaction().block(timeout);
    }

    /**
     * Ends a transaction
     * @param transaction The transaction object.
     * @param commit A boolean value of <code>true</code> indicates transaction to be committed. A value of
     * <code>false</code> indicates a transaction rollback.
     */
    public void endTransaction(Transaction transaction, boolean commit) {
        asynClient.endTransaction(transaction, commit).block(timeout);
    }
}
