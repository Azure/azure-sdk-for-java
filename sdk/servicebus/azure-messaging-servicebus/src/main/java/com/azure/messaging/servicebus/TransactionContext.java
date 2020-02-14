// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;

/**
 * To be able to handle transactions.
 */
public class TransactionContext {
    /**
     *
     * @return transaction id
     */
    public ByteBuffer getTransactionId() {
        return null;
    }

    /**
     * Commit the transaction.
     */
    public void commit() { }

    /**
     * rollback the transaction
     */
    public void rollback() { }
}

