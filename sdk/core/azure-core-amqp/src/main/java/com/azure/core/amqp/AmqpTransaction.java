// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.implementation.TransactionCoordinator;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents transaction in message broker. {@link TransactionCoordinator} uses this to perform many message broker
 * operation as unit of work.
 *
 * @see TransactionCoordinator
 */
public class AmqpTransaction {

    private final ByteBuffer transactionId;

    /**
     * Creates {@link AmqpTransaction} given {@code transactionId}.
     *
     * @param transactionId for this transaction
     *
     * @throws NullPointerException if {@code transactionId} is null.
     */
    public AmqpTransaction(ByteBuffer transactionId) {
        Objects.requireNonNull(transactionId, "'transactionId' cannot be null.");

        this.transactionId = transactionId;
    }

    /**
     * Get {@code transactionId} for this transaction.
     *
     * @return transactionId.
     */
    public ByteBuffer getTransactionId() {
        return transactionId;
    }

    /**
     * String representation of the transaction id.
     *
     * @return string representation of the transaction id.
     */
    public String toString() {
        return new String(transactionId.array(), StandardCharsets.UTF_8);
    }
}
