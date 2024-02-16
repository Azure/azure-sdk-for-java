// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNull;

public class AmqpResponseCodeTest {
    /**
     * Verifies that we can parse the AmqpResponseCode an integer.
     */
    @Test
    public void createFromInteger() {
        int forbidden = 403;
        AmqpResponseCode expected = AmqpResponseCode.FORBIDDEN;

        AmqpResponseCode actual = AmqpResponseCode.fromValue(forbidden);

        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 42, 1024 })
    public void createFromInvalidInteger(int notDefined) {
        assertNull(AmqpResponseCode.fromValue(notDefined));
    }

    /**
     * Verifies that we can parse the AmqpResponseCode an integer.
     */
    @Test
    public void returnsCorrectValue() {
        int expected = 404;
        AmqpResponseCode forbidden = AmqpResponseCode.NOT_FOUND;

        Assertions.assertEquals(expected, forbidden.getValue());
    }
}
