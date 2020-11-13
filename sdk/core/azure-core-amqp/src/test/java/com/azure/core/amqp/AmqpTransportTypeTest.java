// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AmqpTransportTypeTest {

    /**
     * Verifies that we can parse the transport type from string
     */
    @Test
    public void createFromString() {
        String socketString = "Amqpwebsockets";
        AmqpTransportType actual = AmqpTransportType.fromString(socketString);
        Assertions.assertEquals(AmqpTransportType.AMQP_WEB_SOCKETS, actual);
    }

    /**
     * Verifies that an exception is thrown when an unknown transport type string is passed.
     */
    @Test
    public void illegalTransportTypeString() {
        String socketString = "AmqpNonExistent";

        assertThrows(IllegalArgumentException.class, () -> {
            AmqpTransportType actual = AmqpTransportType.fromString(socketString);
        });
    }
}
