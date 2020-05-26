// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.nio.ByteBuffer;

/**
 * Represents transaction.
 */
public class AmqpTransaction {

    private final ByteBuffer transactionId;

    /**
     *
     * @param transactionId for this transaction
     */
    public AmqpTransaction(ByteBuffer transactionId) {
        this.transactionId = transactionId;
    }

    /**
     *
     * @return transaction
     */
    public ByteBuffer getTransactionId() {
        return transactionId;
    }

}
