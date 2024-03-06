// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This have test for {@link AmqpTransaction}.
 */
public class AmqpTransactionTest {

    private static final byte[] TRANSACTION_ID = "1".getBytes();

    @Test
    public void nullTransactionId() {
        assertThrows(NullPointerException.class, () -> new AmqpTransaction(null));
    }

    @Test
    public void transactionCreation() {
        // Arrange
        ByteBuffer transactionId = ByteBuffer.wrap(TRANSACTION_ID);

        // Act
        AmqpTransaction actual = new AmqpTransaction(transactionId);

        // Assert
        assertNotNull(actual);
        assertArrayEquals(TRANSACTION_ID, actual.getTransactionId().array());
    }
}
