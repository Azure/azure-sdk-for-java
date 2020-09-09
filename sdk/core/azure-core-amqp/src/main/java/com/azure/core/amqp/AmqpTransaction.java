// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents transaction in message broker. It does not do any network operation on its own. It contains meta data
 * about transaction such as transaction id.
 *
 */
public class AmqpTransaction {

    private final ByteBuffer transactionId;

    /**
     * Creates {@link AmqpTransaction} given {@code transactionId}.
     *
     * @param transactionId The id for this transaction.
     *
     * @throws NullPointerException if {@code transactionId} is null.
     */
    public AmqpTransaction(ByteBuffer transactionId) {
        this.transactionId = Objects.requireNonNull(transactionId, "'transactionId' cannot be null.");
    }

    /**
     * Gets the id for this transaction.
     *
     * @return The id for this transaction.
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
