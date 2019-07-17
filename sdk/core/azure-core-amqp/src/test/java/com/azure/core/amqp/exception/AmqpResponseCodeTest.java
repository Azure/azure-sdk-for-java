// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AmqpResponseCodeTest {
    /**
     * Verifies that we can parse the AmqpResponseCode an integer.
     */
    @Test
    public void createFromInteger() {
        int forbidden = 403;
        AmqpResponseCode expected = AmqpResponseCode.FORBIDDEN;

        AmqpResponseCode actual = AmqpResponseCode.fromValue(forbidden);

        assertEquals(expected, actual);
    }

    /**
     * Verifies that we can parse the AmqpResponseCode an integer.
     */
    @Test
    public void returnsCorrectValue() {
        int expected = 404;
        AmqpResponseCode forbidden = AmqpResponseCode.NOT_FOUND;

        assertEquals(expected, forbidden.getValue());
    }
}
