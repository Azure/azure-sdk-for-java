// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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

    /**
     * String representation of the transaction id.
     * @return string representation of the transaction id.
     */
    public String toString() {
        return new String(transactionId.array(), Charset.defaultCharset());
    }
}
